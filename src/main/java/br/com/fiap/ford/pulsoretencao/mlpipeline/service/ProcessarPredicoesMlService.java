package br.com.fiap.ford.pulsoretencao.mlpipeline.service;

import br.com.fiap.ford.pulsoretencao.ml.api.MlBffPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlFeaturesRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.service.MlPredictionService;
import br.com.fiap.ford.pulsoretencao.mlpipeline.config.ProcessarPredicoesMlProperties;
import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.PredicaoResultadoEntity;
import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.VinShareFeatureSnapshotEntity;
import br.com.fiap.ford.pulsoretencao.mlpipeline.dto.ProcessarPredicoesResult;
import br.com.fiap.ford.pulsoretencao.mlpipeline.repository.PredicaoResultadoRepository;
import br.com.fiap.ford.pulsoretencao.mlpipeline.repository.VinShareFeatureSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProcessarPredicoesMlService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessarPredicoesMlService.class);

	private final VinShareFeatureSnapshotRepository snapshotRepository;
	private final PredicaoResultadoRepository predicaoResultadoRepository;
	private final MlPredictionService mlPredictionService;
	private final ProcessarPredicoesMlProperties properties;
	private final ObjectMapper objectMapper;
	private final JdbcTemplate jdbcTemplate;
	private final TransactionTemplate transactionTemplate;
	private final String modelVersion;

	public ProcessarPredicoesMlService(
			VinShareFeatureSnapshotRepository snapshotRepository,
			PredicaoResultadoRepository predicaoResultadoRepository,
			MlPredictionService mlPredictionService,
			ProcessarPredicoesMlProperties properties,
			ObjectMapper objectMapper,
			JdbcTemplate jdbcTemplate,
			TransactionTemplate transactionTemplate,
			@org.springframework.beans.factory.annotation.Value("${ford.ml.model-version:churn_pos_venda_rf_calibrated}") String modelVersion) {
		this.snapshotRepository = snapshotRepository;
		this.predicaoResultadoRepository = predicaoResultadoRepository;
		this.mlPredictionService = mlPredictionService;
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate = transactionTemplate;
		this.modelVersion = modelVersion;
	}

	public ProcessarPredicoesResult processarPendentes() {
		List<VinShareFeatureSnapshotEntity> snapshots = transactionTemplate.execute(status ->
				snapshotRepository.reservePendingForPrediction(
						properties.getBatchSize(),
						properties.getRetryDelayMinutes())
		);

		if (snapshots == null || snapshots.isEmpty()) {
			LOGGER.info("Nenhum snapshot elegivel para predicao.");
			return ProcessarPredicoesResult.vazio();
		}

		int success = 0;
		int failed = 0;
		for (VinShareFeatureSnapshotEntity snapshot : snapshots) {
			boolean processed = Boolean.TRUE.equals(transactionTemplate.execute(status ->
					processarSnapshotIsolado(snapshot)
			));
			if (processed) {
				success++;
			} else {
				failed++;
			}
		}

		LOGGER.info("Lote de predicao finalizado. reservados={} sucesso={} falha={}",
				snapshots.size(), success, failed);
		return new ProcessarPredicoesResult(snapshots.size(), success, failed);
	}

	private boolean processarSnapshotIsolado(VinShareFeatureSnapshotEntity snapshot) {
		try {
			MlFeaturesRequest featuresRequest = objectMapper.readValue(snapshot.getPayloadPredict(), MlFeaturesRequest.class);
			MlBffPredictResponse bffPredictResponse = mlPredictionService.predict(
					new MlPredictRequest(featuresRequest, snapshot.getModelo()),
					null
			);
			MlPredictResponse response = bffPredictResponse.ml();
			if (response == null) {
				throw new IllegalStateException("Resposta ML vazia para snapshot " + snapshot.getId());
			}

			PredicaoResultadoEntity entity = new PredicaoResultadoEntity();
			entity.setSnapshotId(snapshot.getId());
			entity.setVeiculoId(snapshot.getVeiculoId());
			entity.setClienteId(snapshot.getClienteId());
			entity.setVinHash(snapshot.getVinHash());
			entity.setPerfil(response.perfilPrevisto());
			entity.setRisco(normalizarRisco(response.riskLevel()));
			entity.setScore(response.churnProbability() == null ? null : BigDecimal.valueOf(response.churnProbability()));
			entity.setMotivoPrincipal(response.motivo());
			entity.setAcaoRecomendada(response.acaoRecomendada());
			entity.setCanalRecomendado(response.canalRecomendado());
			entity.setModeloVersao(modelVersion);
			entity.setPayloadResposta(toJson(response));
			predicaoResultadoRepository.save(entity);

			callVoidFunction("select ml.mark_prediction_completed(?)", snapshot.getId());
			return true;
		} catch (Exception exception) {
			String message = sanitizarErro(exception);
			callVoidFunction(
					"select ml.mark_prediction_failed(?, ?, ?)",
					snapshot.getId(),
					message,
					properties.getMaxAttempts()
			);
			LOGGER.warn("Falha ao processar snapshot_id={}: {}", snapshot.getId(), message);
			return false;
		}
	}

	private void callVoidFunction(String sql, Object... params) {
		jdbcTemplate.query(sql, rs -> {
		}, params);
	}

	private String sanitizarErro(Exception exception) {
		String message = exception.getMessage();
		if (message == null || message.isBlank()) {
			return "erro_desconhecido";
		}
		return message.length() > 1000 ? message.substring(0, 1000) : message;
	}

	private String normalizarRisco(String riskLevel) {
		if ("high".equalsIgnoreCase(riskLevel)) {
			return "alto";
		}
		if ("medium".equalsIgnoreCase(riskLevel)) {
			return "medio";
		}
		return "baixo";
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException exception) {
			return "{\"erro\":\"falha_ao_serializar_payload_ml\"}";
		}
	}
}

package br.com.fiap.ford.pulsoretencao.ml.service;

import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictItem;
import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchProcessResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlFeaturesRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictResponse;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MlBatchPredictionService {

	private static final String RESERVE_SQL = """
			select
			    q.id as queue_id,
			    q.snapshot_id,
			    s.veiculo_id,
			    s.cliente_id,
			    q.vin_hash,
			    s.payload_predict
			from public.ml_feature_refresh_queue q
			join public.vin_share_feature_snapshots s on s.id = q.snapshot_id
			where q.status = 'ready_to_predict'
			order by q.criado_em
			limit ?
			for update skip locked
			""";

	private final JdbcTemplate jdbcTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;
	private final MlPredictionService mlPredictionService;
	private final String modeloVersao;

	public MlBatchPredictionService(
			JdbcTemplate jdbcTemplate,
			TransactionTemplate transactionTemplate,
			MlPredictionService mlPredictionService,
			@Value("${ford.ml.model-version:churn_pos_venda_rf_calibrated}") String modeloVersao) {
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = new ObjectMapper();
		this.mlPredictionService = mlPredictionService;
		this.modeloVersao = modeloVersao;
	}

	public MlBatchProcessResponse processarLote(int limit, String tokenOverride) {
		List<QueueItem> reservados = reservar(limit);
		if (reservados.isEmpty()) {
			return new MlBatchProcessResponse(0, 0, 0, "empty", "Nenhum snapshot pronto para predicao.");
		}

		try {
			MlBatchPredictRequest request = montarRequest(reservados);
			MlBatchPredictResponse response = mlPredictionService.predictBatch(request, tokenOverride);
			Map<String, MlPredictResponse> respostas = indexarPorReferencia(response.items());
			salvarResultados(reservados, respostas);

			return new MlBatchProcessResponse(
					reservados.size(),
					reservados.size(),
					0,
					"completed",
					"Lote processado com sucesso."
			);
		} catch (RuntimeException exception) {
			marcarFalha(reservados, exception);
			throw exception;
		}
	}

	private List<QueueItem> reservar(int limit) {
		List<QueueItem> reservados = transactionTemplate.execute(status -> {
			List<QueueItem> items = jdbcTemplate.query(RESERVE_SQL, this::mapQueueItem, limit);
			if (!items.isEmpty()) {
				jdbcTemplate.batchUpdate(
						"""
						update public.ml_feature_refresh_queue
						set status = 'prediction_processing',
						    erro = null,
						    atualizado_em = current_timestamp
						where id = ?
						""",
						items,
						items.size(),
						(ps, item) -> ps.setLong(1, item.queueId())
				);
			}
			return items;
		});

		return reservados == null ? List.of() : reservados;
	}

	private QueueItem mapQueueItem(ResultSet rs, int rowNum) throws SQLException {
		return new QueueItem(
				rs.getLong("queue_id"),
				rs.getLong("snapshot_id"),
				readNullableLong(rs, "veiculo_id"),
				readNullableLong(rs, "cliente_id"),
				rs.getString("vin_hash"),
				rs.getString("payload_predict")
		);
	}

	private Long readNullableLong(ResultSet rs, String column) throws SQLException {
		long value = rs.getLong(column);
		return rs.wasNull() ? null : value;
	}

	private MlBatchPredictRequest montarRequest(List<QueueItem> items) {
		List<MlBatchPredictItem> requestItems = new ArrayList<>();
		for (QueueItem item : items) {
			requestItems.add(new MlBatchPredictItem(
					item.referenceId(),
					lerFeatures(item)
			));
		}
		return new MlBatchPredictRequest(requestItems);
	}

	private MlFeaturesRequest lerFeatures(QueueItem item) {
		try {
			return objectMapper.readValue(item.payloadPredict(), MlFeaturesRequest.class);
		} catch (JsonProcessingException exception) {
			throw new BadRequestException(
					"payload_predict invalido para queue_id " + item.queueId() + ": " + exception.getOriginalMessage());
		}
	}

	private Map<String, MlPredictResponse> indexarPorReferencia(List<MlPredictResponse> respostas) {
		Map<String, MlPredictResponse> index = new HashMap<>();
		for (MlPredictResponse resposta : respostas) {
			if (resposta == null || !StringUtils.hasText(resposta.referenceId())) {
				throw new BadRequestException("FastAPI ML retornou item sem reference_id.");
			}
			if (index.put(resposta.referenceId(), resposta) != null) {
				throw new BadRequestException("FastAPI ML retornou reference_id duplicado: " + resposta.referenceId());
			}
		}
		return index;
	}

	private void salvarResultados(List<QueueItem> reservados, Map<String, MlPredictResponse> respostas) {
		transactionTemplate.executeWithoutResult(status -> {
			for (QueueItem item : reservados) {
				MlPredictResponse resposta = respostas.get(item.referenceId());
				if (resposta == null) {
					throw new BadRequestException("FastAPI ML nao retornou reference_id " + item.referenceId());
				}

				jdbcTemplate.update(
						"""
						insert into public.predicao_resultados (
						    queue_id,
						    snapshot_id,
						    veiculo_id,
						    cliente_id,
						    vin_hash,
						    perfil,
						    risco,
						    score,
						    motivo_principal,
						    acao_recomendada,
						    canal_recomendado,
						    modelo_versao,
						    payload_resposta,
						    executado_em
						)
						values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, cast(? as jsonb), current_timestamp)
						on conflict (queue_id)
						do update set
						    snapshot_id = excluded.snapshot_id,
						    veiculo_id = excluded.veiculo_id,
						    cliente_id = excluded.cliente_id,
						    vin_hash = excluded.vin_hash,
						    perfil = excluded.perfil,
						    risco = excluded.risco,
						    score = excluded.score,
						    motivo_principal = excluded.motivo_principal,
						    acao_recomendada = excluded.acao_recomendada,
						    canal_recomendado = excluded.canal_recomendado,
						    modelo_versao = excluded.modelo_versao,
						    payload_resposta = excluded.payload_resposta,
						    executado_em = current_timestamp
						""",
						item.queueId(),
						item.snapshotId(),
						item.veiculoId(),
						item.clienteId(),
						item.vinHash(),
						resposta.perfilPrevisto(),
						resposta.riskLevel(),
						resposta.churnProbability(),
						motivoPrincipal(resposta),
						resposta.acaoRecomendada(),
						resposta.canalRecomendado(),
						modeloVersao,
						toJson(resposta)
				);

				jdbcTemplate.update(
						"""
						update public.ml_feature_refresh_queue
						set status = 'completed',
						    erro = null,
						    processado_em = current_timestamp,
						    atualizado_em = current_timestamp
						where id = ?
						""",
						item.queueId()
				);
			}
		});
	}

	private String motivoPrincipal(MlPredictResponse resposta) {
		if (StringUtils.hasText(resposta.motivo())) {
			return resposta.motivo();
		}
		return "Modelo ML retornou risco " + resposta.riskLevel()
				+ " com probabilidade de churn " + resposta.churnProbability() + ".";
	}

	private String toJson(MlPredictResponse resposta) {
		try {
			return objectMapper.writeValueAsString(resposta);
		} catch (JsonProcessingException exception) {
			throw new BadRequestException("Nao foi possivel serializar resposta da FastAPI ML.");
		}
	}

	private void marcarFalha(List<QueueItem> reservados, RuntimeException exception) {
		String erro = sanitizarErro(exception);
		transactionTemplate.executeWithoutResult(status ->
				jdbcTemplate.batchUpdate(
						"""
						update public.ml_feature_refresh_queue
						set status = 'failed',
						    tentativas = tentativas + 1,
						    erro = ?,
						    processado_em = current_timestamp,
						    atualizado_em = current_timestamp
						where id = ?
						""",
						reservados,
						reservados.size(),
						(ps, item) -> {
							ps.setString(1, erro);
							ps.setLong(2, item.queueId());
						}
				)
		);
	}

	private String sanitizarErro(RuntimeException exception) {
		String message = exception.getMessage();
		if (!StringUtils.hasText(message)) {
			message = exception.getClass().getSimpleName();
		}
		String sanitized = message.replaceAll("[\\r\\n\\t]+", " ");
		return sanitized.substring(0, Math.min(sanitized.length(), 1000));
	}

	private record QueueItem(
			long queueId,
			long snapshotId,
			Long veiculoId,
			Long clienteId,
			String vinHash,
			String payloadPredict
	) {
		String referenceId() {
			return "queue-" + queueId;
		}
	}
}

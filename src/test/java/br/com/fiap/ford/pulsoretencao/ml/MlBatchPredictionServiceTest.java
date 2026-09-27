package br.com.fiap.ford.pulsoretencao.ml;

import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.service.MlBatchPredictionService;
import br.com.fiap.ford.pulsoretencao.ml.service.MlPredictionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "ford.ml.model-version=test-model")
@ActiveProfiles("test")
class MlBatchPredictionServiceTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void limparDadosDoTeste() {
		jdbcTemplate.update("delete from predicao_resultados where vin_hash like 'TEST-BATCH-%'");
		jdbcTemplate.update("delete from ml_feature_refresh_queue where vin_hash like 'TEST-BATCH-%'");
		jdbcTemplate.update("delete from vin_share_feature_snapshots where vin_hash like 'TEST-BATCH-%'");
	}

	@Test
	void processaLoteNaoVazioEPersisteResultado() {
		long queueId = inserirFila("TEST-BATCH-001", "2020-01-01");
		MlPredictionService predictionService = mock(MlPredictionService.class);
		when(predictionService.predictBatch(any())).thenReturn(new MlBatchPredictResponse(List.of(
				new MlPredictResponse("queue-" + queueId, "sim", 0.87, "alto", "Cliente esquecido",
						Map.of("Cliente esquecido", 0.8), "Contato", "Sem retorno", "telefone", List.of())
		)));

		MlBatchPredictionService service = new MlBatchPredictionService(
				jdbcTemplate, transactionTemplate, objectMapper, predictionService, "test-model");
		var response = service.processarLote(1);

		assertThat(response.processados()).isEqualTo(1);
		assertThat(response.status()).isEqualTo("completed");
		assertThat(jdbcTemplate.queryForObject("select status from ml_feature_refresh_queue where id = ?",
				String.class, queueId)).isEqualTo("done");
		assertThat(jdbcTemplate.queryForObject("select count(*) from predicao_resultados where queue_id = ?",
				Integer.class, queueId)).isEqualTo(1);
		verify(predictionService).predictBatch(any());
	}

	@Test
	void falhaDoServicoMarcaFilaComoFailed() {
		long queueId = inserirFila("TEST-BATCH-002", "2020-01-02");
		MlPredictionService predictionService = mock(MlPredictionService.class);
		when(predictionService.predictBatch(any())).thenThrow(new IllegalStateException("servico indisponivel"));
		MlBatchPredictionService service = new MlBatchPredictionService(
				jdbcTemplate, transactionTemplate, objectMapper, predictionService, "test-model");

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.processarLote(1))
				.isInstanceOf(IllegalStateException.class);

		assertThat(jdbcTemplate.queryForObject("select status from ml_feature_refresh_queue where id = ?",
				String.class, queueId)).isEqualTo("failed");
		assertThat(jdbcTemplate.queryForObject("select tentativas from ml_feature_refresh_queue where id = ?",
				Integer.class, queueId)).isEqualTo(1);
	}

	@Test
	void referenciaDuplicadaOuAusenteFazFilaFalhar() {
		long duplicatedQueueId = inserirFila("TEST-BATCH-003", "2020-01-03");
		MlPredictResponse duplicated = resposta("queue-" + duplicatedQueueId);
		MlPredictionService duplicatedService = mock(MlPredictionService.class);
		when(duplicatedService.predictBatch(any())).thenReturn(new MlBatchPredictResponse(List.of(duplicated, duplicated)));
		assertThatThrownBy(() -> new MlBatchPredictionService(jdbcTemplate, transactionTemplate, objectMapper,
				duplicatedService, "test-model").processarLote(1)).isInstanceOf(RuntimeException.class);

		long missingQueueId = inserirFila("TEST-BATCH-004", "2020-01-04");
		MlPredictionService missingService = mock(MlPredictionService.class);
		when(missingService.predictBatch(any())).thenReturn(new MlBatchPredictResponse(List.of()));
		assertThatThrownBy(() -> new MlBatchPredictionService(jdbcTemplate, transactionTemplate, objectMapper,
				missingService, "test-model").processarLote(1)).isInstanceOf(RuntimeException.class);
		assertThat(jdbcTemplate.queryForObject("select status from ml_feature_refresh_queue where id = ?",
				String.class, duplicatedQueueId)).isEqualTo("failed");
		assertThat(jdbcTemplate.queryForObject("select status from ml_feature_refresh_queue where id = ?",
				String.class, missingQueueId)).isEqualTo("failed");
	}

	private MlPredictResponse resposta(String referenceId) {
		return new MlPredictResponse(referenceId, "sim", 0.87, "alto", "Cliente esquecido",
				Map.of("Cliente esquecido", 0.8), "Contato", "Sem retorno", "telefone", List.of());
	}

	private long inserirFila(String vinHash, String dataCorte) {
		String payload = """
				{"ano_modelo":2020,"qtde_revisoes_ate_corte":2,"meses_desde_ultimo_servico_ate_corte":14.2,
				"meses_relacionamento_ate_corte":48.0,"n_dealers_usados_ate_corte":1,"km_max_ate_corte":48200.0,
				"pct_agenda_ate_corte":0.65,"intervalo_medio_revisoes_dias_ate_corte":220.0,
				"dias_ate_primeira_revisao":180,"idade_veiculo_meses_ate_corte":54.0,"modelo":"KA"}
				""".replace("\n", "");
		jdbcTemplate.update("""
				insert into vin_share_feature_snapshots
				(vin_hash, data_corte, modelo, ano_modelo, payload_predict, payload_hash)
				values (?, ?, 'KA', 2020, ?, 'test-hash')
				""", vinHash, LocalDate.parse(dataCorte), payload);
		Long snapshotId = jdbcTemplate.queryForObject(
				"select id from vin_share_feature_snapshots where vin_hash = ?", Long.class, vinHash);
		jdbcTemplate.update("""
				insert into ml_feature_refresh_queue (vin_hash, data_corte, snapshot_id, status)
				values (?, ?, ?, 'done')
				""", vinHash, LocalDate.parse(dataCorte), snapshotId);
		return jdbcTemplate.queryForObject("select id from ml_feature_refresh_queue where vin_hash = ?",
				Long.class, vinHash);
	}
}

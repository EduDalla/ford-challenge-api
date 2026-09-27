package br.com.fiap.ford.pulsoretencao.ml;

import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictItem;
import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlFeaturesRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictRequest;
import br.com.fiap.ford.pulsoretencao.ml.service.MlPredictionService;
import br.com.fiap.ford.pulsoretencao.shared.exception.ExternalServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadGateway;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MlPredictionServiceTest {

	private RestClient restClient;
	private MockRestServiceServer server;
	private MlPredictionService service;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://ml.test");
		server = MockRestServiceServer.bindTo(builder).build();
		restClient = builder.build();
		service = new MlPredictionService(restClient, "ml-test-token");
	}

	@AfterEach
	void verifyRequests() {
		server.verify();
	}

	@Test
	void predicaoIndividualEncaminhaTokenERetornaResultado() {
		server.expect(requestTo("http://ml.test/predict"))
				.andRespond(withSuccess("""
						{"reference_id":"ref-1","prediction":"churn","churn_probability":0.82,
						"risk_level":"high","perfil_previsto":"inativo","acao_recomendada":"Ligar",
						"motivo":"Sem revisão recente","canal_recomendado":"telefone"}
						""", MediaType.APPLICATION_JSON));

		var response = service.predict(request());

		assertEquals("high", response.ml().riskLevel());
		assertEquals("alto", response.missao().risco());
	}

	@Test
	void respostaHttpDoUpstreamViraBadGateway() {
		server.expect(requestTo("http://ml.test/predict"))
				.andRespond(withBadGateway().body("detalhe interno da FastAPI"));

		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> service.predict(request()));

		assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
		org.junit.jupiter.api.Assertions.assertFalse(exception.getMessage().contains("FastAPI"));
	}

	@Test
	void loteComQuantidadeDiferenteDeItensERejeitado() {
		server.expect(requestTo("http://ml.test/predict-batch"))
				.andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));

		MlBatchPredictRequest request = new MlBatchPredictRequest(List.of(
				new MlBatchPredictItem("ref-1", request().features())));

		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> service.predictBatch(request));

		assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
	}

	@Test
	void tokenAusenteIndicaServicoIndisponivel() {
		MlPredictionService semToken = new MlPredictionService(restClient, "");

		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> semToken.predict(request()));

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
	}

	private MlPredictRequest request() {
		return new MlPredictRequest(new MlFeaturesRequest(
				2020, 2, 14.2, 48.0, 1, 48200.0, 0.65, 220.0, 180, 54.0, "KA"), "Ford Ka");
	}
}

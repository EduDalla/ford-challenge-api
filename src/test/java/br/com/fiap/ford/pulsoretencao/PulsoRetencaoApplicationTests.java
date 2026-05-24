package br.com.fiap.ford.pulsoretencao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"app.demo-mode=true",
		"app.ml.demo-token=demo-test-token",
		"ford.ml.service-token=service-test-token"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PulsoRetencaoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void healthEndpointsSaoPublicos() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));

		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void endpointTokenServicoEPublicoMasRejeitaCredenciaisInvalidas() throws Exception {
		mockMvc.perform(post("/api/v1/auth/service-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"clientId":"cliente-inexistente","clientSecret":"segredo-invalido"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void endpointMlSemTokenRetornaErroDeAutorizacao() throws Exception {
		mockMvc.perform(post("/api/v1/ml/predicoes/processar-lote"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void endpointMlRejeitaHeaderDemoInvalido() throws Exception {
		mockMvc.perform(post("/api/v1/ml/predicoes/processar-lote")
						.header("X-ML-Demo-Token", "token-invalido"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void endpointMlAceitaHeaderDemoQuandoDemoModeEstaAtivo() throws Exception {
		mockMvc.perform(post("/api/v1/ml/predicoes/processar-lote")
						.header("X-ML-Demo-Token", "demo-test-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("empty"));
	}

}

package br.com.fiap.ford.pulsoretencao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void endpointMlRejeitaHeaderDemoInvalido() throws Exception {
		mockMvc.perform(post("/api/v1/ml/predicoes/processar-lote")
					.header("X-ML-Demo-Token", "token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void endpointMlAceitaHeaderDemoQuandoDemoModeEstaAtivo() throws Exception {
		mockMvc.perform(post("/api/v1/ml/predicoes/processar-lote")
						.header("X-ML-Demo-Token", "demo-test-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("empty"));
	}

	@Test
	void analistaNaoAcessaClientes() throws Exception {
		mockMvc.perform(get("/api/v1/clientes").with(authentication(
				new UsernamePasswordAuthenticationToken("analista", null,
						java.util.List.of(new SimpleGrantedAuthority("ROLE_ANALISTA"))))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void clienteTemCriacaoConsultaAtualizacaoESoftDelete() throws Exception {
		String body = """
				{"nome":"Cliente de teste","email":"cliente.teste@example.com","telefone":"11999999999",
				"documento":"DOC-TESTE-001","segmento":"Pos-venda","nivelRisco":"MEDIO","ativo":true}
				""";

		var admin = authentication(new UsernamePasswordAuthenticationToken("admin", null,
				java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

		String location = mockMvc.perform(post("/api/v1/clientes").with(admin)
					.contentType(MediaType.APPLICATION_JSON)
					.content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get(location).with(admin))
				.andExpect(status().isOk());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(location).with(admin)
					.contentType(MediaType.APPLICATION_JSON)
					.content(body.replace("Cliente de teste", "Cliente atualizado")))
				.andExpect(status().isOk());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(location).with(admin))
				.andExpect(status().isNoContent());
		mockMvc.perform(get(location).with(admin))
				.andExpect(status().isNotFound());
	}

}

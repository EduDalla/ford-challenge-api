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
import br.com.fiap.ford.pulsoretencao.profile.domain.PerfilProfile;
import br.com.fiap.ford.pulsoretencao.profile.domain.Profile;
import java.util.UUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

	@Test
	void informacaoTemCriacaoConsultaEValidacao() throws Exception {
		var admin = authentication(new UsernamePasswordAuthenticationToken("admin", null,
				java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
		String body = """
				{"nome":"Informacao teste automatizado","descricao":"Descricao para o consultor","ativo":true}
				""";

		String location = mockMvc.perform(post("/api/v1/informacoes").with(admin)
					.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get(location).with(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Informacao teste automatizado"));
		mockMvc.perform(post("/api/v1/informacoes").with(admin)
					.contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\",\"descricao\":\"\",\"ativo\":null}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void interacaoTemCriacaoEConsultaPorCliente() throws Exception {
		var admin = authentication(new UsernamePasswordAuthenticationToken("admin", null,
				java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
		String body = """
				{"tipo":"TELEFONE","descricao":"Contato de teste","resultado":"Retorno agendado","dataInteracao":"2026-09-26T10:00:00"}
				""";

		mockMvc.perform(post("/api/v1/clientes/1/interacoes").with(admin)
					.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
		mockMvc.perform(get("/api/v1/clientes/1/interacoes").with(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void radarDeMissoesERecursoInexistenteTemStatusEspecifico() throws Exception {
		Profile adminProfile = mock(Profile.class);
		when(adminProfile.getId()).thenReturn(UUID.randomUUID());
		when(adminProfile.getPerfil()).thenReturn(PerfilProfile.ADMIN);
		var admin = authentication(new UsernamePasswordAuthenticationToken(
				adminProfile, null,
				java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

		mockMvc.perform(get("/api/v1/radar/prioridades").with(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].codigoCartao").value("CARD-001"));
		mockMvc.perform(get("/api/v1/missoes/999999").with(admin))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void errosHttpMantemContratoEStatusEspecificos() throws Exception {
		mockMvc.perform(get("/api/v1/clientes"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", "Bearer"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.path").value("/api/v1/clientes"));

		var admin = authentication(new UsernamePasswordAuthenticationToken("admin", null,
				java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
		mockMvc.perform(get("/api/v1/clientes?nivelRisco=INVALID").with(admin))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
		mockMvc.perform(post("/api/v1/clientes").with(admin)
					.contentType(MediaType.TEXT_PLAIN).content("texto"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.status").value(415));
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/health"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.status").value(405));
	}

	@Test
	void openApiExponeContratoDeErrosEAutenticacao() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.schemas.ErrorResponse").exists())
				.andExpect(jsonPath("$.paths['/api/v1/clientes'].get.responses['401']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/clientes'].get.responses['429']").exists())
				.andExpect(jsonPath("$.paths['/api/v1/clientes'].get.responses['401'].headers.WWW-Authenticate").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/service-token'].post.security").doesNotExist());
	}

}

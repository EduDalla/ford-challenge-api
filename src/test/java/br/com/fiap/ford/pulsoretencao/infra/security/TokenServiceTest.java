package br.com.fiap.ford.pulsoretencao.infra.security;

import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenServiceTest {

	private static final String SECRET = "service-secret";

    @Test
    void serviceTokenExpiraDuasHorasAposInstanteDoRelogio() throws Exception {
        Instant agora = Instant.parse("2026-09-26T12:00:00Z");
        TokenService service = new TokenService(
                SECRET,
                "",
                "",
                "",
                "authenticated",
                Clock.fixed(agora, ZoneOffset.UTC),
                false
        );
        ApiClient client = new ApiClient();
        Field clientId = ApiClient.class.getDeclaredField("clientId");
        clientId.setAccessible(true);
        clientId.set(client, "client-test");

        assertEquals(agora.plusSeconds(2 * 60 * 60), service.gerarTokenServico(client).expiraEm());
    }

	@Test
	void rejeitaAudienceIncorretaNoTokenTecnico() {
		TokenService service = new TokenService(SECRET, "", "", "", "authenticated",
				Clock.systemUTC(), false);
		String token = JWT.create()
				.withIssuer("API Ford")
				.withAudience("audience-incorreta")
				.withSubject("client-test")
				.withClaim("type", "service")
				.withExpiresAt(Instant.now().plusSeconds(3600))
				.sign(Algorithm.HMAC256(SECRET));

		assertThrows(TokenService.BadCredentialsJwtException.class, () -> service.validarToken(token));
	}

	@Test
	void rejeitaClaimsObrigatoriasAusentesOuTipoIncorreto() {
		TokenService service = new TokenService(SECRET, "", "", "", "authenticated",
				Clock.systemUTC(), false);
		String semExpiracao = JWT.create()
				.withIssuer("API Ford")
				.withAudience("pulso-retencao-api")
				.withSubject("client-test")
				.withClaim("type", "service")
				.sign(Algorithm.HMAC256(SECRET));
		String tipoIncorreto = JWT.create()
				.withIssuer("API Ford")
				.withAudience("pulso-retencao-api")
				.withSubject("client-test")
				.withClaim("type", "user")
				.withExpiresAt(Instant.now().plusSeconds(3600))
				.sign(Algorithm.HMAC256(SECRET));

		assertThrows(TokenService.BadCredentialsJwtException.class, () -> service.validarToken(semExpiracao));
		assertThrows(TokenService.BadCredentialsJwtException.class, () -> service.validarToken(tipoIncorreto));
	}

	@Test
	void rejeitaAssinaturaAdulterada() {
		TokenService service = new TokenService(SECRET, "", "", "", "authenticated",
				Clock.systemUTC(), false);
		String token = JWT.create()
				.withIssuer("API Ford")
				.withAudience("pulso-retencao-api")
				.withSubject("client-test")
				.withClaim("type", "service")
				.withExpiresAt(Instant.now().plusSeconds(3600))
				.sign(Algorithm.HMAC256("outro-secret"));

		assertThrows(TokenService.BadCredentialsJwtException.class, () -> service.validarToken(token));
	}
}

package br.com.fiap.ford.pulsoretencao.infra.security;

import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenServiceTest {

    @Test
    void serviceTokenExpiraDuasHorasAposInstanteDoRelogio() throws Exception {
        Instant agora = Instant.parse("2026-09-26T12:00:00Z");
        TokenService service = new TokenService(
                "service-secret",
                "",
                "",
                "",
                "authenticated",
                Clock.fixed(agora, ZoneOffset.UTC)
        );
        ApiClient client = new ApiClient();
        Field clientId = ApiClient.class.getDeclaredField("clientId");
        clientId.setAccessible(true);
        clientId.set(client, "client-test");

        assertEquals(agora.plusSeconds(2 * 60 * 60), service.gerarTokenServico(client).expiraEm());
    }
}

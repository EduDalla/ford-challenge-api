package br.com.fiap.ford.pulsoretencao.infra.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityConfigurationsTest {

    @Test
    void rejeitaDemoModeNoProfileProd() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.profiles.active", "prod");

        assertThrows(IllegalStateException.class,
                () -> new SecurityConfigurations(true, "demo-token", environment));
    }
}

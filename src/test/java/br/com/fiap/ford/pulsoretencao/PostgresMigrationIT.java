package br.com.fiap.ford.pulsoretencao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@ActiveProfiles("postgres")
class PostgresMigrationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pulso_test")
            .withUsername("pulso_test")
            .withPassword("pulso_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration-postgres");
        registry.add("app.jobs.refresh-feature-snapshots.enabled", () -> "false");
        registry.add("app.jobs.processar-predicoes-ml.enabled", () -> "false");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void inicializaMigrationsPostgresEAplicafixTures() {
        Integer profiles = jdbcTemplate.queryForObject("select count(*) from public.profiles", Integer.class);
        Integer permissions = jdbcTemplate.queryForObject("select count(*) from private.permissions", Integer.class);

        assertTrue(profiles >= 3);
        assertTrue(permissions >= 1);
    }
}

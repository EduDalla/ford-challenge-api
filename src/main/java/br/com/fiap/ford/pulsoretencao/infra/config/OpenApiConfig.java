package br.com.fiap.ford.pulsoretencao.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI pulsoRetencaoOpenAPI() {
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
				.schemaRequirement("bearerAuth", new SecurityScheme()
						.name("bearerAuth")
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT"))
				.info(new Info()
						.title("Pulso Retenção API")
						.version("v1")
						.description("API REST para acompanhar clientes em risco de evasão e registrar interações de retenção."));
	}
}

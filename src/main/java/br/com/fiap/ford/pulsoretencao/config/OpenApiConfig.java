package br.com.fiap.ford.pulsoretencao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI pulsoRetencaoOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Pulso Retenção API")
						.version("v1")
						.description("API REST para acompanhar clientes em risco de evasão e registrar interações de retenção."));
	}
}

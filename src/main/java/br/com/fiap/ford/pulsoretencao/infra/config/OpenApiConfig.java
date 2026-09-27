package br.com.fiap.ford.pulsoretencao.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
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
				.components(new io.swagger.v3.oas.models.Components()
						.addSchemas("ErrorResponse", new Schema<>()
								.type("object")
								.addProperties("timestamp", new Schema<>().type("string").format("date-time"))
								.addProperties("status", new Schema<>().type("integer"))
								.addProperties("error", new Schema<>().type("string"))
								.addProperties("message", new Schema<>().type("string"))
								.addProperties("path", new Schema<>().type("string")))
						.addResponses("Unauthorized", new ApiResponse().description("Bearer ausente ou inválido"))
						.addResponses("Forbidden", new ApiResponse().description("Permissão insuficiente"))
						.addResponses("TooManyRequests", new ApiResponse().description("Limite de requisições excedido")))
				.info(new Info()
						.title("Pulso Retenção API")
						.version("v1")
						.description("API REST para acompanhar clientes em risco de evasão e registrar interações de retenção. "
								+ "Usuários humanos usam JWT Supabase; clientes técnicos usam service token com scopes; "
								+ "ML usa X-ML-Service-Token apenas na integração BFF → FastAPI."));
	}
}

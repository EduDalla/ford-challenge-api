package br.com.fiap.ford.pulsoretencao.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.headers.Header;
import org.springdoc.core.customizers.OpenApiCustomizer;
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
								.addProperties("path", new Schema<>().type("string"))
								.addProperties("fields", new Schema<>().type("object").additionalProperties(true)))
						.addResponses("Unauthorized", new ApiResponse().description("Bearer ausente ou inválido"))
						.addResponses("Forbidden", new ApiResponse().description("Permissão insuficiente"))
						.addResponses("TooManyRequests", new ApiResponse().description("Limite de requisições excedido")))
				.info(new Info()
						.title("Pulso Retenção API")
						.version("v1")
						.description("API REST para acompanhar clientes em risco de evasão e registrar interações de retenção. "
								+ "Usuários humanos usam JWT Supabase; clientes técnicos usam service token com scopes; "
								+ "ML usa X-ML-Service-Token apenas na integração BFF → FastAPI. "
								+ "Fora de prod, o Swagger e o OpenAPI são públicos; os endpoints ML também aceitam "
								+ "X-ML-Demo-Token quando DEMO_MODE=true."));
	}

	@Bean
	public OpenApiCustomizer errorResponsesCustomizer() {
		return openAPI -> {
			if (!openAPI.getComponents().getSchemas().containsKey("ErrorResponse")) {
				openAPI.getComponents().addSchemas("ErrorResponse", errorSchema());
			}
			openAPI.getPaths().forEach((path, pathItem) -> {
				if (!path.startsWith("/api/")) {
					return;
				}
				pathItem.readOperations().forEach(operation -> {
					if (!path.equals("/api/v1/auth/service-token")) {
						operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
					}
					ApiResponses responses = operation.getResponses();
					addIfMissing(responses, "400", response("Requisição inválida."));
					addIfMissing(responses, "401", response("Bearer ausente ou inválido.")
							.addHeaderObject("WWW-Authenticate", new Header().description("Bearer")));
					addIfMissing(responses, "403", response("Permissão insuficiente."));
					addIfMissing(responses, "404", response("Recurso não encontrado."));
					addIfMissing(responses, "405", response("Método HTTP não suportado."));
					addIfMissing(responses, "415", response("Tipo de mídia não suportado."));
					addIfMissing(responses, "429", response("Limite de requisições excedido.")
							.addHeaderObject("Retry-After", new Header().description("Segundos até nova tentativa.")));
					addIfMissing(responses, "500", response("Erro interno inesperado."));
				});
			});
		};
	}

	private Schema<Object> errorSchema() {
		return new Schema<>()
				.type("object")
				.addProperties("timestamp", new Schema<>().type("string").format("date-time"))
				.addProperties("status", new Schema<>().type("integer"))
				.addProperties("error", new Schema<>().type("string"))
				.addProperties("message", new Schema<>().type("string"))
				.addProperties("path", new Schema<>().type("string"))
				.addProperties("fields", new Schema<>().type("object").additionalProperties(true));
	}

	private void addIfMissing(ApiResponses responses, String status, ApiResponse response) {
		if (!responses.containsKey(status)) {
			responses.addApiResponse(status, response);
		}
	}

	private ApiResponse response(String description) {
		return new ApiResponse()
				.description(description)
				.content(new Content().addMediaType("application/json",
						new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
	}
}

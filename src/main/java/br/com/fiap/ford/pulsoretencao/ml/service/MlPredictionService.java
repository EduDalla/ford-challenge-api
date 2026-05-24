package br.com.fiap.ford.pulsoretencao.ml.service;

import br.com.fiap.ford.pulsoretencao.ml.api.MlBffPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlBatchPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlMissaoResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictRequest;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictResponse;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MlPredictionService {

	private final RestClient fordMlRestClient;
	private final String serviceToken;

	public MlPredictionService(RestClient fordMlRestClient,
			@Value("${ford.ml.service-token:}") String serviceToken) {
		this.fordMlRestClient = fordMlRestClient;
		this.serviceToken = serviceToken;
	}

	public MlBffPredictResponse predict(MlPredictRequest request) {
		try {
			MlPredictResponse ml = fordMlRestClient.post()
					.uri("/predict")
					.headers(this::applyMlAuth)
					.body(request)
					.retrieve()
					.body(MlPredictResponse.class);

			if (ml == null) {
				throw new BadRequestException("FastAPI ML retornou resposta vazia.");
			}

			return new MlBffPredictResponse(ml, toMissao(request, ml));
		} catch (RestClientResponseException exception) {
			throw new BadRequestException(
					"FastAPI ML retornou " + exception.getStatusCode().value() + ": "
							+ sanitizeBody(exception.getResponseBodyAsString()));
		} catch (ResourceAccessException exception) {
			throw new BadRequestException("FastAPI ML indisponivel ou timeout ao chamar /predict.");
		} catch (RestClientException exception) {
			throw new BadRequestException("Falha ao chamar FastAPI ML: " + exception.getMessage());
		}
	}

	public MlBatchPredictResponse predictBatch(MlBatchPredictRequest request) {
		try {
			MlBatchPredictResponse response = fordMlRestClient.post()
					.uri("/predict-batch")
					.headers(this::applyMlAuth)
					.body(request)
					.retrieve()
					.body(MlBatchPredictResponse.class);

			if (response == null || response.items() == null) {
				throw new BadRequestException("FastAPI ML retornou resposta batch vazia.");
			}
			if (response.items().size() != request.items().size()) {
				throw new BadRequestException("FastAPI ML retornou quantidade diferente de itens no batch.");
			}
			return response;
		} catch (RestClientResponseException exception) {
			throw new BadRequestException(
					"FastAPI ML retornou " + exception.getStatusCode().value() + ": "
							+ sanitizeBody(exception.getResponseBodyAsString()));
		} catch (ResourceAccessException exception) {
			throw new BadRequestException("FastAPI ML indisponivel ou timeout ao chamar /predict-batch.");
		} catch (RestClientException exception) {
			throw new BadRequestException("Falha ao chamar FastAPI ML: " + exception.getMessage());
		}
	}

	private void applyMlAuth(HttpHeaders headers) {
		if (StringUtils.hasText(serviceToken)) {
			headers.set("X-ML-Service-Token", serviceToken.trim());
			return;
		}
		throw new BadRequestException("Token ML ausente. Configure FORD_ML_SERVICE_TOKEN.");
	}

	private MlMissaoResponse toMissao(MlPredictRequest request, MlPredictResponse ml) {
		int score = (int) Math.round(nullToZero(ml.churnProbability()) * 100);
		String risco = toRiscoNegocio(ml.riskLevel());
		String prioridade = switch (risco) {
			case "alto" -> "P1";
			case "medio" -> "P2";
			default -> "P3";
		};
		String perfil = toPerfilNegocio(ml.perfilPrevisto());
		String acao = StringUtils.hasText(ml.acaoRecomendada())
				? ml.acaoRecomendada()
				: fallbackAcao(ml.perfilPrevisto(), risco);
		String modelo = StringUtils.hasText(request.modeloVeiculo())
				? request.modeloVeiculo()
				: request.features().modelo();

		List<String> sinais = new ArrayList<>();
		sinais.add("Score ML de abandono: " + score + "/100");
		sinais.add("Perfil previsto pelo modelo: " + perfil);
		sinais.add("Risco priorizado pelo BFF Java: " + risco);

		return new MlMissaoResponse(
				"CARD-ML-DEMO",
				risco,
				score,
				prioridade,
				perfil,
				motivoPrincipal(risco, perfil),
				acao,
				sinais,
				modelo
		);
	}

	private double nullToZero(Double value) {
		return value == null ? 0.0 : value;
	}

	private String toRiscoNegocio(String riskLevel) {
		if ("high".equalsIgnoreCase(riskLevel)) {
			return "alto";
		}
		if ("medium".equalsIgnoreCase(riskLevel)) {
			return "medio";
		}
		return "baixo";
	}

	private String toPerfilNegocio(String perfil) {
		if (perfil == null) {
			return "Perfil nao informado";
		}
		return switch (perfil) {
			case "baixo_engajamento" -> "Cliente de baixo engajamento";
			case "inativo" -> "Cliente inativo";
			case "multidealer" -> "Cliente multidealer";
			case "recorrente" -> "Cliente recorrente";
			default -> perfil;
		};
	}

	private String fallbackAcao(String perfil, String risco) {
		return switch (perfil == null ? "" : perfil) {
			case "baixo_engajamento" -> "Priorizar contato consultivo e explicar beneficios da rede Ford.";
			case "inativo" -> "Ativar recuperacao imediata com agendamento prioritario.";
			case "multidealer" -> "Reforcar vinculo com a concessionaria atual e oferecer acompanhamento dedicado.";
			case "recorrente" -> "Manter relacionamento ativo e registrar convite para a proxima manutencao.";
			default -> risco.equals("alto")
					? "Contato imediato para recuperar o cliente antes da evasao."
					: "Monitorar e programar contato de retencao.";
		};
	}

	private String motivoPrincipal(String risco, String perfil) {
		return "Modelo ML classificou o cliente como " + perfil + " com risco " + risco + " de abandono.";
	}

	private String sanitizeBody(String body) {
		if (!StringUtils.hasText(body)) {
			return "sem corpo de erro";
		}
		return body.length() > 500 ? body.substring(0, 500) : body;
	}
}

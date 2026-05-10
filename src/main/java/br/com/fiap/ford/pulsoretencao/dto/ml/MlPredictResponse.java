package br.com.fiap.ford.pulsoretencao.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record MlPredictResponse(
		String prediction,

		@JsonProperty("churn_probability")
		Double churnProbability,

		@JsonProperty("risk_level")
		String riskLevel,

		@JsonProperty("perfil_previsto")
		String perfilPrevisto,

		@JsonProperty("probabilidades_perfil")
		Map<String, Double> probabilidadesPerfil,

		@JsonProperty("acao_recomendada")
		String acaoRecomendada,

		@JsonProperty("historico_problemas")
		List<Map<String, Object>> historicoProblemas
) {
}

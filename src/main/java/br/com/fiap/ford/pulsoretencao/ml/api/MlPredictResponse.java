package br.com.fiap.ford.pulsoretencao.ml.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record MlPredictResponse(
		@JsonProperty("reference_id")
		String referenceId,

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

		String motivo,

		@JsonProperty("canal_recomendado")
		String canalRecomendado,

		@JsonProperty("historico_problemas")
		List<Map<String, Object>> historicoProblemas
) {
}

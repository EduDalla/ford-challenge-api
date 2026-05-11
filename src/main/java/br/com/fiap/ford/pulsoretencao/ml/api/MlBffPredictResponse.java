package br.com.fiap.ford.pulsoretencao.ml.api;

public record MlBffPredictResponse(
		MlPredictResponse ml,
		MlMissaoResponse missao
) {
}

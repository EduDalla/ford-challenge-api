package br.com.fiap.ford.pulsoretencao.dto.ml;

public record MlBffPredictResponse(
		MlPredictResponse ml,
		MlMissaoResponse missao
) {
}

package br.com.fiap.ford.pulsoretencao.ml.api;

import java.util.List;

public record MlBatchPredictResponse(
		List<MlPredictResponse> items
) {
}

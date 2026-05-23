package br.com.fiap.ford.pulsoretencao.ml.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MlBatchPredictRequest(
		@Valid
		@NotEmpty
		List<MlBatchPredictItem> items
) {
}

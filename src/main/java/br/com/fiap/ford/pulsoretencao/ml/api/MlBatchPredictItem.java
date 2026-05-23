package br.com.fiap.ford.pulsoretencao.ml.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record MlBatchPredictItem(
		@JsonProperty("reference_id")
		String referenceId,

		@Valid
		@NotNull
		MlFeaturesRequest features
) {
}

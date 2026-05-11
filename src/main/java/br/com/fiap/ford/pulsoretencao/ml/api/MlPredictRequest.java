package br.com.fiap.ford.pulsoretencao.ml.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record MlPredictRequest(
		@Valid
		@NotNull
		MlFeaturesRequest features,

		@Schema(example = "Ka")
		@JsonProperty("modelo_veiculo")
		String modeloVeiculo
) {
}

package br.com.fiap.ford.pulsoretencao.missao.api;

import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarMissaoRequest(
		Long clienteId,
		@Valid
		MissaoClienteRequest cliente,
		Long veiculoId,
		@Valid
		MissaoVeiculoRequest veiculo,

		@Valid
		@NotNull
		MlPredictRequest predicao,

		@Size(max = 40)
		String codigoCartao,

		@Size(max = 80)
		String prazo,

		@DecimalMin("0.0")
		BigDecimal valorPotencial,

		@DecimalMin("0.0")
		BigDecimal impactoVinShareEstimado
) {
}

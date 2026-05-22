package br.com.fiap.ford.pulsoretencao.missao.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegistrarResultadoMissaoRequest(
		Boolean compareceu,
		Boolean servicoPago,

		@DecimalMin("0.0")
		BigDecimal receitaEstimada,

		@DecimalMin("0.0")
		BigDecimal impactoVinShare,

		@Size(max = 255)
		String proximoPasso
) {
}

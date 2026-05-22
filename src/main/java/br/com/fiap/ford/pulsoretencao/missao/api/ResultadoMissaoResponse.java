package br.com.fiap.ford.pulsoretencao.missao.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResultadoMissaoResponse(
		Boolean compareceu,
		Boolean servicoPago,
		BigDecimal receitaEstimada,
		BigDecimal impactoVinShare,
		String proximoPasso,
		LocalDateTime atualizadoEm
) {
}

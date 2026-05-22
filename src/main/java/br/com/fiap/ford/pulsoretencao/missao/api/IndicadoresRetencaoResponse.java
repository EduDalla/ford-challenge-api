package br.com.fiap.ford.pulsoretencao.missao.api;

import java.math.BigDecimal;

public record IndicadoresRetencaoResponse(
		long total,
		long altoRisco,
		long contatosFeitos,
		long agendados,
		long recuperados,
		long perdidos,
		int taxaRecuperacao,
		int taxaAgendamento,
		BigDecimal impactoVinShareEstimado,
		BigDecimal receitaPotencialRecuperada
) {
}

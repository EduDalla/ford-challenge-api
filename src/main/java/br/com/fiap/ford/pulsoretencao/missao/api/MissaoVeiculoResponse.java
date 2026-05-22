package br.com.fiap.ford.pulsoretencao.missao.api;

import java.time.LocalDate;

public record MissaoVeiculoResponse(
		String modelo,
		Integer ano,
		String vinSimulado,
		Integer kmAtual,
		LocalDate ultimaRevisao,
		Integer diasSemServico,
		String statusGarantia
) {
}

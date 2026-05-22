package br.com.fiap.ford.pulsoretencao.missao.api;

import java.time.LocalDateTime;

public record HistoricoAcaoResponse(
		Long id,
		String tipo,
		String canal,
		String observacao,
		ResultadoMissaoResponse resultado,
		LocalDateTime criadoEm
) {
}

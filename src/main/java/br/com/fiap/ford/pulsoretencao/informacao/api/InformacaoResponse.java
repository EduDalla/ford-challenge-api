package br.com.fiap.ford.pulsoretencao.informacao.api;

import java.time.LocalDateTime;

public record InformacaoResponse(
		Long id,
		String nome,
		String descricao,
		Boolean ativo,
		LocalDateTime criadoEm,
		LocalDateTime atualizadoEm
) {
}

package br.com.fiap.ford.pulsoretencao.dto;

import br.com.fiap.ford.pulsoretencao.model.NivelRisco;

import java.time.LocalDateTime;

public record ClienteResponse(
		Long id,
		String nome,
		String email,
		String telefone,
		String documento,
		String segmento,
		NivelRisco nivelRisco,
		Boolean ativo,
		LocalDateTime criadoEm,
		LocalDateTime atualizadoEm,
		Long totalInteracoes
) {
}

package br.com.fiap.ford.pulsoretencao.cliente.api;

import br.com.fiap.ford.pulsoretencao.cliente.domain.NivelRisco;

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
		LocalDateTime excluidoEm
) {
}

package br.com.fiap.ford.pulsoretencao.interacao.api;

import br.com.fiap.ford.pulsoretencao.interacao.TipoInteracao;

import java.time.LocalDateTime;

public record InteracaoResponse(
		Long id,
		Long clienteId,
		String clienteNome,
		TipoInteracao tipo,
		String descricao,
		String resultado,
		LocalDateTime dataInteracao
) {
}

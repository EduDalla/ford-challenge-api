package br.com.fiap.ford.pulsoretencao.autenticacao.api;

import jakarta.validation.constraints.NotBlank;

public record DadosAutenticacaoServico(
		@NotBlank
		String clientId,

		@NotBlank
		String clientSecret
) {
}

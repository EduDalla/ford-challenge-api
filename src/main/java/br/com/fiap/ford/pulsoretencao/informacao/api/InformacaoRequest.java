package br.com.fiap.ford.pulsoretencao.informacao.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InformacaoRequest(
		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
		String nome,

		@NotBlank(message = "A descrição é obrigatória")
		@Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
		String descricao,

		Boolean ativo
) {
}

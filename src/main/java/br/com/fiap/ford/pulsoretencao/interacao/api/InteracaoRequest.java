package br.com.fiap.ford.pulsoretencao.interacao.api;

import br.com.fiap.ford.pulsoretencao.interacao.domain.TipoInteracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record InteracaoRequest(
		@NotNull(message = "O tipo da interação é obrigatório")
		TipoInteracao tipo,

		@NotBlank(message = "A descrição é obrigatória")
		@Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
		String descricao,

		@NotBlank(message = "O resultado é obrigatório")
		@Size(max = 120, message = "O resultado deve ter no máximo 120 caracteres")
		String resultado,

		@NotNull(message = "A data da interação é obrigatória")
		@PastOrPresent(message = "A data da interação não pode estar no futuro")
		LocalDateTime dataInteracao
) {
}

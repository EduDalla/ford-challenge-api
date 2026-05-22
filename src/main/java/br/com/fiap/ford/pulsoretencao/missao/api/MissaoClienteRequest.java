package br.com.fiap.ford.pulsoretencao.missao.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MissaoClienteRequest(
		@NotBlank
		@Size(max = 120)
		String nome,

		@NotBlank
		@Email
		@Size(max = 150)
		String email,

		@Size(max = 20)
		String telefone,

		@NotBlank
		@Size(max = 30)
		String documento,

		@Size(max = 80)
		String segmento,

		@Size(max = 80)
		String canalPreferido,

		@Size(max = 80)
		String cidade,

		@Size(max = 500)
		String historicoResumo
) {
}

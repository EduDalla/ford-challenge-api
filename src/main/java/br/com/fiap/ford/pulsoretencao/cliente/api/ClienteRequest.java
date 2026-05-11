package br.com.fiap.ford.pulsoretencao.cliente.api;

import br.com.fiap.ford.pulsoretencao.cliente.domain.NivelRisco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
		String nome,

		@NotBlank(message = "O email é obrigatório")
		@Email(message = "O email deve ser válido")
		@Size(max = 150, message = "O email deve ter no máximo 150 caracteres")
		String email,

		@Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
		String telefone,

		@NotBlank(message = "O documento é obrigatório")
		@Size(max = 30, message = "O documento deve ter no máximo 30 caracteres")
		String documento,

		@Size(max = 80, message = "O segmento deve ter no máximo 80 caracteres")
		String segmento,

		@NotNull(message = "O nível de risco é obrigatório")
		NivelRisco nivelRisco,

		Boolean ativo
) {
}

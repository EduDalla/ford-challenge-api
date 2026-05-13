package br.com.fiap.ford.pulsoretencao.informacaouser.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InformacaoUserRequest(
		@NotNull(message = "O userId é obrigatório")
		Long userId,

		@NotNull(message = "O informacaoId é obrigatório")
		Long informacaoId,

		@NotNull(message = "A dataAlerta é obrigatória")
		LocalDate dataAlerta
) {
}

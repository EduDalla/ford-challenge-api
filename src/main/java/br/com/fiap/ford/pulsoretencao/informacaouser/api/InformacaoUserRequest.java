package br.com.fiap.ford.pulsoretencao.informacaouser.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record InformacaoUserRequest(
		@NotNull(message = "O userId e obrigatorio")
		UUID userId,

		@NotNull(message = "O informacaoId e obrigatorio")
		Long informacaoId,

		@NotNull(message = "A dataAlerta e obrigatoria")
		LocalDate dataAlerta
) {
}

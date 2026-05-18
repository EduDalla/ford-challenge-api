package br.com.fiap.ford.pulsoretencao.informacaouser.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InformacaoUserResponse(
		Long id,
		UUID userId,
		Long informacaoId,
		LocalDate dataAlerta,
		LocalDateTime criadoEm
) {
}

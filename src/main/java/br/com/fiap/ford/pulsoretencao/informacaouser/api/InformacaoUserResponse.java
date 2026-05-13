package br.com.fiap.ford.pulsoretencao.informacaouser.api;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InformacaoUserResponse(
		Long id,
		Long userId,
		Long informacaoId,
		LocalDate dataAlerta,
		LocalDateTime criadoEm
) {
}

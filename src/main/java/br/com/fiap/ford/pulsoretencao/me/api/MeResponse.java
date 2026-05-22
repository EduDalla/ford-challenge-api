package br.com.fiap.ford.pulsoretencao.me.api;

import java.util.UUID;

public record MeResponse(
		UUID id,
		String nome,
		String email,
		String perfil
) {
}

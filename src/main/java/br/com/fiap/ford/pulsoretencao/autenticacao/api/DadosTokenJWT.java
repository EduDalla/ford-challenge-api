package br.com.fiap.ford.pulsoretencao.autenticacao.api;

import java.time.Instant;

public record DadosTokenJWT(
		String token,
		String tipo,
		Instant expiraEm
) {
}

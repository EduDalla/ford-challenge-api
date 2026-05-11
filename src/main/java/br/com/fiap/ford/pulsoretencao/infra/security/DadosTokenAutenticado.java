package br.com.fiap.ford.pulsoretencao.infra.security;

import java.util.List;

public record DadosTokenAutenticado(
		String subject,
		String type,
		String role,
		List<String> scopes
) {
}

package br.com.fiap.ford.pulsoretencao.missao.api;

public record MissaoClienteResponse(
		Long id,
		String nome,
		String canalPreferido,
		String telefoneMascarado,
		String cidade,
		String historicoResumo
) {
}

package br.com.fiap.ford.pulsoretencao.ml.api;

public record MlBatchProcessResponse(
		int reservados,
		int processados,
		int falhas,
		String status,
		String mensagem
) {
}

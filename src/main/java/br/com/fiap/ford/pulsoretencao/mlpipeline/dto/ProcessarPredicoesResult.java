package br.com.fiap.ford.pulsoretencao.mlpipeline.dto;

public record ProcessarPredicoesResult(
		int totalReservados,
		int totalSucesso,
		int totalFalha
) {
	public static ProcessarPredicoesResult vazio() {
		return new ProcessarPredicoesResult(0, 0, 0);
	}
}

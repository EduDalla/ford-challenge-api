package br.com.fiap.ford.pulsoretencao.missao.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MissaoVeiculoRequest(
		@NotBlank
		@Size(max = 80)
		String modelo,

		@NotNull
		Integer ano,

		@NotBlank
		@Size(max = 40)
		String vinSimulado,

		@NotNull
		@PositiveOrZero
		Integer kmAtual,

		@NotNull
		@PastOrPresent
		LocalDate ultimaRevisao,

		@NotNull
		@PositiveOrZero
		Integer diasSemServico,

		@NotBlank
		@Size(max = 40)
		String statusGarantia
) {
}

package br.com.fiap.ford.pulsoretencao.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MlFeaturesRequest(
		@NotNull
		@Schema(example = "2020")
		@JsonProperty("ano_modelo")
		Integer anoModelo,

		@NotNull
		@PositiveOrZero
		@Schema(example = "2")
		@JsonProperty("qtde_revisoes_ate_corte")
		Integer qtdeRevisoesAteCorte,

		@NotNull
		@PositiveOrZero
		@Schema(example = "14.2")
		@JsonProperty("meses_desde_ultimo_servico_ate_corte")
		Double mesesDesdeUltimoServicoAteCorte,

		@NotNull
		@PositiveOrZero
		@Schema(example = "48.0")
		@JsonProperty("meses_relacionamento_ate_corte")
		Double mesesRelacionamentoAteCorte,

		@NotNull
		@PositiveOrZero
		@Schema(example = "1")
		@JsonProperty("n_dealers_usados_ate_corte")
		Integer nDealersUsadosAteCorte,

		@NotNull
		@PositiveOrZero
		@Schema(example = "48200")
		@JsonProperty("km_max_ate_corte")
		Double kmMaxAteCorte,

		@NotNull
		@PositiveOrZero
		@DecimalMax("1.0")
		@Schema(example = "0.65")
		@JsonProperty("pct_agenda_ate_corte")
		Double pctAgendaAteCorte,

		@NotNull
		@PositiveOrZero
		@Schema(example = "220.0")
		@JsonProperty("intervalo_medio_revisoes_dias_ate_corte")
		Double intervaloMedioRevisoesDiasAteCorte,

		@NotNull
		@PositiveOrZero
		@Schema(example = "180")
		@JsonProperty("dias_ate_primeira_revisao")
		Integer diasAtePrimeiraRevisao,

		@NotNull
		@PositiveOrZero
		@Schema(example = "54.0")
		@JsonProperty("idade_veiculo_meses_ate_corte")
		Double idadeVeiculoMesesAteCorte,

		@NotBlank
		@Schema(example = "KA")
		String modelo
) {
}

package br.com.fiap.ford.pulsoretencao.mlpipeline.domain;

import br.com.fiap.ford.pulsoretencao.mlpipeline.enums.FeatureSnapshotPredictionStatus;
import br.com.fiap.ford.pulsoretencao.mlpipeline.persistence.converter.FeatureSnapshotPredictionStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vin_share_feature_snapshots")
public class VinShareFeatureSnapshotEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "veiculo_id")
	private Long veiculoId;

	@Column(name = "cliente_id")
	private Long clienteId;

	@Column(name = "vin_hash", nullable = false, length = 80)
	private String vinHash;

	@Column(name = "data_corte", nullable = false)
	private LocalDate dataCorte;

	@Column(name = "modelo", nullable = false)
	private String modelo;

	@Column(name = "ano_modelo", nullable = false)
	private Integer anoModelo;

	@Column(name = "qtde_revisoes_ate_corte")
	private Integer qtdeRevisoesAteCorte;

	@Column(name = "meses_desde_ultimo_servico_ate_corte")
	private BigDecimal mesesDesdeUltimoServicoAteCorte;

	@Column(name = "meses_relacionamento_ate_corte")
	private BigDecimal mesesRelacionamentoAteCorte;

	@Column(name = "n_dealers_usados_ate_corte")
	private Integer nDealersUsadosAteCorte;

	@Column(name = "km_max_ate_corte")
	private BigDecimal kmMaxAteCorte;

	@Column(name = "pct_agenda_ate_corte")
	private BigDecimal pctAgendaAteCorte;

	@Column(name = "intervalo_medio_revisoes_dias_ate_corte")
	private BigDecimal intervaloMedioRevisoesDiasAteCorte;

	@Column(name = "dias_ate_primeira_revisao")
	private Integer diasAtePrimeiraRevisao;

	@Column(name = "idade_veiculo_meses_ate_corte")
	private BigDecimal idadeVeiculoMesesAteCorte;

	@Column(name = "payload_predict", nullable = false, columnDefinition = "jsonb")
	private String payloadPredict;

	@Convert(converter = FeatureSnapshotPredictionStatusConverter.class)
	@Column(name = "status_predicao", nullable = false, length = 30)
	private FeatureSnapshotPredictionStatus statusPredicao;

	@Column(name = "tentativas_predicao", nullable = false)
	private Integer tentativasPredicao;

	@Column(name = "erro_predicao")
	private String erroPredicao;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	@Column(name = "predito_em")
	private LocalDateTime preditoEm;

	public Long getId() {
		return id;
	}

	public Long getVeiculoId() {
		return veiculoId;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public String getVinHash() {
		return vinHash;
	}

	public LocalDate getDataCorte() {
		return dataCorte;
	}

	public String getModelo() {
		return modelo;
	}

	public Integer getAnoModelo() {
		return anoModelo;
	}

	public Integer getQtdeRevisoesAteCorte() {
		return qtdeRevisoesAteCorte;
	}

	public BigDecimal getMesesDesdeUltimoServicoAteCorte() {
		return mesesDesdeUltimoServicoAteCorte;
	}

	public BigDecimal getMesesRelacionamentoAteCorte() {
		return mesesRelacionamentoAteCorte;
	}

	public Integer getnDealersUsadosAteCorte() {
		return nDealersUsadosAteCorte;
	}

	public BigDecimal getKmMaxAteCorte() {
		return kmMaxAteCorte;
	}

	public BigDecimal getPctAgendaAteCorte() {
		return pctAgendaAteCorte;
	}

	public BigDecimal getIntervaloMedioRevisoesDiasAteCorte() {
		return intervaloMedioRevisoesDiasAteCorte;
	}

	public Integer getDiasAtePrimeiraRevisao() {
		return diasAtePrimeiraRevisao;
	}

	public BigDecimal getIdadeVeiculoMesesAteCorte() {
		return idadeVeiculoMesesAteCorte;
	}

	public String getPayloadPredict() {
		return payloadPredict;
	}

	public FeatureSnapshotPredictionStatus getStatusPredicao() {
		return statusPredicao;
	}

	public Integer getTentativasPredicao() {
		return tentativasPredicao;
	}

	public String getErroPredicao() {
		return erroPredicao;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public LocalDateTime getPreditoEm() {
		return preditoEm;
	}
}

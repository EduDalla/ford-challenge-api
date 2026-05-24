package br.com.fiap.ford.pulsoretencao.mlpipeline.domain;

import br.com.fiap.ford.pulsoretencao.mlpipeline.enums.MlFeatureRefreshQueueStatus;
import br.com.fiap.ford.pulsoretencao.mlpipeline.persistence.converter.MlFeatureRefreshQueueStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ml_feature_refresh_queue")
public class MlFeatureRefreshQueueEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vin_hash", nullable = false, length = 80)
	private String vinHash;

	@Column(name = "data_corte", nullable = false)
	private LocalDate dataCorte;

	@Convert(converter = MlFeatureRefreshQueueStatusConverter.class)
	@Column(name = "status", nullable = false, length = 30)
	private MlFeatureRefreshQueueStatus status;

	@Column(name = "motivo", nullable = false, length = 80)
	private String motivo;

	@Column(name = "erro")
	private String erro;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	@Column(name = "processado_em")
	private LocalDateTime processadoEm;

	public Long getId() {
		return id;
	}

	public String getVinHash() {
		return vinHash;
	}

	public LocalDate getDataCorte() {
		return dataCorte;
	}

	public MlFeatureRefreshQueueStatus getStatus() {
		return status;
	}

	public String getMotivo() {
		return motivo;
	}

	public String getErro() {
		return erro;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public LocalDateTime getProcessadoEm() {
		return processadoEm;
	}
}

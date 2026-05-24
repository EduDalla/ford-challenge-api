package br.com.fiap.ford.pulsoretencao.mlpipeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vin_share_servicos")
public class VinShareServicoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "vin_hash", nullable = false, length = 80)
	private String vinHash;

	@Column(name = "cliente_id")
	private Long clienteId;

	@Column(name = "dealer_code", length = 40)
	private String dealerCode;

	@Column(name = "model_name", nullable = false, length = 80)
	private String modelName;

	@Column(name = "model_year", nullable = false)
	private Integer modelYear;

	@Column(name = "sales_date")
	private LocalDate salesDate;

	@Column(name = "delivery_date")
	private LocalDate deliveryDate;

	@Column(name = "service_date", nullable = false)
	private LocalDate serviceDate;

	@Column(name = "km", precision = 12, scale = 2)
	private BigDecimal km;

	@Column(name = "service_type", length = 80)
	private String tipoServico;

	@Column(name = "agenda_flag")
	private Boolean agendaFlag;

	@Column(name = "origem")
	private String origem;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	public Long getId() {
		return id;
	}

	public String getVinHash() {
		return vinHash;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public String getDealerCode() {
		return dealerCode;
	}

	public String getModelName() {
		return modelName;
	}

	public Integer getModelYear() {
		return modelYear;
	}

	public LocalDate getSalesDate() {
		return salesDate;
	}

	public LocalDate getDeliveryDate() {
		return deliveryDate;
	}

	public LocalDate getServiceDate() {
		return serviceDate;
	}

	public BigDecimal getKm() {
		return km;
	}

	public String getTipoServico() {
		return tipoServico;
	}

	public Boolean getAgendaFlag() {
		return agendaFlag;
	}

	public String getOrigem() {
		return origem;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}
}

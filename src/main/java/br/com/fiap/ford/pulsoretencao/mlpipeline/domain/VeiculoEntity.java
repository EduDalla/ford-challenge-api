package br.com.fiap.ford.pulsoretencao.mlpipeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "veiculos")
public class VeiculoEntity {

	@Id
	private Long id;

	@Column(name = "cliente_id")
	private Long clienteId;

	@Column(name = "vin_hash")
	private String vinHash;

	@Column(name = "vin_simulado")
	private String vinSimulado;

	@Column(name = "modelo")
	private String modelo;

	@Column(name = "ano")
	private Integer anoModelo;

	@Column(name = "sales_date")
	private LocalDate salesDate;

	@Column(name = "delivery_date")
	private LocalDate deliveryDate;

	public Long getId() {
		return id;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public String getVinHash() {
		return vinHash;
	}

	public String getVinSimulado() {
		return vinSimulado;
	}

	public String getModelo() {
		return modelo;
	}

	public Integer getAnoModelo() {
		return anoModelo;
	}

	public LocalDate getSalesDate() {
		return salesDate;
	}

	public LocalDate getDeliveryDate() {
		return deliveryDate;
	}
}

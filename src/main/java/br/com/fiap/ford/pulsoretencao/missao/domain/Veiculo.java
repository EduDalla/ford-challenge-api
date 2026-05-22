package br.com.fiap.ford.pulsoretencao.missao.domain;

import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "veiculos")
public class Veiculo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@Column(nullable = false, length = 80)
	private String modelo;

	@Column(nullable = false)
	private Integer ano;

	@Column(name = "vin_simulado", nullable = false, length = 40)
	private String vinSimulado;

	@Column(name = "km_atual", nullable = false)
	private Integer kmAtual;

	@Column(name = "ultima_revisao", nullable = false)
	private LocalDate ultimaRevisao;

	@Column(name = "dias_sem_servico", nullable = false)
	private Integer diasSemServico;

	@Column(name = "status_garantia", nullable = false, length = 40)
	private String statusGarantia;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	public Veiculo() {
	}

	public Veiculo(Cliente cliente, String modelo, Integer ano, String vinSimulado, Integer kmAtual,
			LocalDate ultimaRevisao, Integer diasSemServico, String statusGarantia) {
		this.cliente = cliente;
		this.modelo = modelo;
		this.ano = ano;
		this.vinSimulado = vinSimulado;
		this.kmAtual = kmAtual;
		this.ultimaRevisao = ultimaRevisao;
		this.diasSemServico = diasSemServico;
		this.statusGarantia = statusGarantia;
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime agora = LocalDateTime.now();
		criadoEm = agora;
		atualizadoEm = agora;
	}

	@PreUpdate
	public void preUpdate() {
		atualizadoEm = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public String getModelo() {
		return modelo;
	}

	public Integer getAno() {
		return ano;
	}

	public String getVinSimulado() {
		return vinSimulado;
	}

	public Integer getKmAtual() {
		return kmAtual;
	}

	public LocalDate getUltimaRevisao() {
		return ultimaRevisao;
	}

	public Integer getDiasSemServico() {
		return diasSemServico;
	}

	public String getStatusGarantia() {
		return statusGarantia;
	}
}

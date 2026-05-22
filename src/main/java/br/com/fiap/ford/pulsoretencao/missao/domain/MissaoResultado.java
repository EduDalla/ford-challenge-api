package br.com.fiap.ford.pulsoretencao.missao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "missao_resultados")
public class MissaoResultado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "missao_id", nullable = false)
	private Missao missao;

	private Boolean compareceu;

	@Column(name = "servico_pago")
	private Boolean servicoPago;

	@Column(name = "receita_estimada", precision = 12, scale = 2)
	private BigDecimal receitaEstimada;

	@Column(name = "impacto_vin_share", precision = 5, scale = 2)
	private BigDecimal impactoVinShare;

	@Column(name = "proximo_passo", length = 255)
	private String proximoPasso;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	public MissaoResultado() {
	}

	public MissaoResultado(Missao missao) {
		this.missao = missao;
	}

	@PrePersist
	@PreUpdate
	public void touch() {
		atualizadoEm = LocalDateTime.now();
	}

	public void atualizar(Boolean compareceu, Boolean servicoPago, BigDecimal receitaEstimada,
			BigDecimal impactoVinShare, String proximoPasso) {
		this.compareceu = compareceu;
		this.servicoPago = servicoPago;
		this.receitaEstimada = receitaEstimada;
		this.impactoVinShare = impactoVinShare;
		this.proximoPasso = proximoPasso;
		this.atualizadoEm = LocalDateTime.now();
	}

	public Boolean getCompareceu() {
		return compareceu;
	}

	public Boolean getServicoPago() {
		return servicoPago;
	}

	public BigDecimal getReceitaEstimada() {
		return receitaEstimada;
	}

	public BigDecimal getImpactoVinShare() {
		return impactoVinShare;
	}

	public String getProximoPasso() {
		return proximoPasso;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}
}

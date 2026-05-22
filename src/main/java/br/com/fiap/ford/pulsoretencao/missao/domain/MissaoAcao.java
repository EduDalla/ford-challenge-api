package br.com.fiap.ford.pulsoretencao.missao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "missao_acoes")
public class MissaoAcao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "missao_id", nullable = false)
	private Missao missao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatusMissao tipo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CanalContato canal;

	@Column(length = 500)
	private String observacao;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	public MissaoAcao() {
	}

	public MissaoAcao(Missao missao, StatusMissao tipo, CanalContato canal, String observacao) {
		this.missao = missao;
		this.tipo = tipo;
		this.canal = canal;
		this.observacao = observacao;
	}

	@PrePersist
	public void prePersist() {
		criadoEm = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public StatusMissao getTipo() {
		return tipo;
	}

	public CanalContato getCanal() {
		return canal;
	}

	public String getObservacao() {
		return observacao;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}
}

package br.com.fiap.ford.pulsoretencao.informacao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "informacoes")
public class Informacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 120)
	private String nome;

	@Column(nullable = false, length = 255)
	private String descricao;

	@Column(nullable = false)
	private Boolean ativo = true;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	public Informacao() {
	}

	public Informacao(String nome, String descricao, Boolean ativo) {
		this.nome = nome;
		this.descricao = descricao;
		this.ativo = ativo != null ? ativo : true;
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime agora = LocalDateTime.now();
		criadoEm = agora;
		atualizadoEm = agora;
		if (ativo == null) {
			ativo = true;
		}
	}

	@PreUpdate
	public void preUpdate() {
		atualizadoEm = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}
}

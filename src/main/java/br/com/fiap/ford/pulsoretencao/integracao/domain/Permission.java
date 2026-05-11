package br.com.fiap.ford.pulsoretencao.integracao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 80)
	private String codigo;

	@Column(nullable = false, length = 255)
	private String descricao;

	@Column(nullable = false)
	private Boolean ativo = true;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	public Permission() {
	}

	@PrePersist
	public void prePersist() {
		criadoEm = LocalDateTime.now();
		if (ativo == null) {
			ativo = true;
		}
	}

	public Long getId() {
		return id;
	}

	public String getCodigo() {
		return codigo;
	}

	public Boolean getAtivo() {
		return ativo;
	}
}

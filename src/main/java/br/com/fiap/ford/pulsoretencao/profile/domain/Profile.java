package br.com.fiap.ford.pulsoretencao.profile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

	@Id
	private UUID id;

	@Column(nullable = false, length = 120)
	private String nome;

	@Column(length = 150)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PerfilProfile perfil;

	@Column(nullable = false)
	private Boolean ativo = true;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	public Profile() {
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public PerfilProfile getPerfil() {
		return perfil;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}
}

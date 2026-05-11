package br.com.fiap.ford.pulsoretencao.integracao.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "api_clients")
public class ApiClient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nome;

	@Column(name = "client_id", nullable = false, unique = true, length = 100)
	private String clientId;

	@Column(name = "client_secret_hash", nullable = false, length = 255)
	private String clientSecretHash;

	@Column(nullable = false)
	private Boolean ativo = true;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	@Column(name = "ultimo_uso_em")
	private LocalDateTime ultimoUsoEm;

	@OneToMany(mappedBy = "apiClient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<ApiClientPermission> permissions = new HashSet<>();

	public ApiClient() {
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

	public void registrarUso() {
		ultimoUsoEm = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecretHash() {
		return clientSecretHash;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public Set<ApiClientPermission> getPermissions() {
		return permissions;
	}
}

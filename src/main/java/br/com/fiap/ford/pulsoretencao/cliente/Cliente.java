package br.com.fiap.ford.pulsoretencao.cliente;

import br.com.fiap.ford.pulsoretencao.interacao.Interacao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String nome;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(length = 20)
	private String telefone;

	@Column(nullable = false, unique = true, length = 30)
	private String documento;

	@Column(length = 80)
	private String segmento;

	@Enumerated(EnumType.STRING)
	@Column(name = "nivel_risco", nullable = false, length = 20)
	private NivelRisco nivelRisco;

	@Column(nullable = false)
	private Boolean ativo = true;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	@OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Interacao> interacoes = new ArrayList<>();

	public Cliente() {
	}

	public Cliente(String nome, String email, String telefone, String documento, String segmento,
			NivelRisco nivelRisco, Boolean ativo) {
		this.nome = nome;
		this.email = email;
		this.telefone = telefone;
		this.documento = documento;
		this.segmento = segmento;
		this.nivelRisco = nivelRisco;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getSegmento() {
		return segmento;
	}

	public void setSegmento(String segmento) {
		this.segmento = segmento;
	}

	public NivelRisco getNivelRisco() {
		return nivelRisco;
	}

	public void setNivelRisco(NivelRisco nivelRisco) {
		this.nivelRisco = nivelRisco;
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

	public List<Interacao> getInteracoes() {
		return interacoes;
	}
}

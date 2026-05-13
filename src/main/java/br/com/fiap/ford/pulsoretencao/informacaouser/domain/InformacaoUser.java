package br.com.fiap.ford.pulsoretencao.informacaouser.domain;

import br.com.fiap.ford.pulsoretencao.informacao.domain.Informacao;
import br.com.fiap.ford.pulsoretencao.usuario.domain.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "informacoes_user")
public class InformacaoUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "informacao_id", nullable = false)
	private Informacao informacao;

	@Column(name = "data_alerta", nullable = false)
	private LocalDate dataAlerta;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	public InformacaoUser() {
	}

	public InformacaoUser(Usuario usuario, Informacao informacao, LocalDate dataAlerta) {
		this.usuario = usuario;
		this.informacao = informacao;
		this.dataAlerta = dataAlerta;
	}

	@PrePersist
	public void prePersist() {
		criadoEm = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Informacao getInformacao() {
		return informacao;
	}

	public void setInformacao(Informacao informacao) {
		this.informacao = informacao;
	}

	public LocalDate getDataAlerta() {
		return dataAlerta;
	}

	public void setDataAlerta(LocalDate dataAlerta) {
		this.dataAlerta = dataAlerta;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}
}

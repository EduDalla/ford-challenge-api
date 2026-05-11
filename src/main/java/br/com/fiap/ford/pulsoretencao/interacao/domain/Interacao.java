package br.com.fiap.ford.pulsoretencao.interacao.domain;

import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
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
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "interacoes")
public class Interacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoInteracao tipo;

	@Column(nullable = false, length = 500)
	private String descricao;

	@Column(nullable = false, length = 120)
	private String resultado;

	@Column(name = "data_interacao", nullable = false)
	private LocalDateTime dataInteracao;

	public Interacao() {
	}

	public Interacao(Cliente cliente, TipoInteracao tipo, String descricao, String resultado,
			LocalDateTime dataInteracao) {
		this.cliente = cliente;
		this.tipo = tipo;
		this.descricao = descricao;
		this.resultado = resultado;
		this.dataInteracao = dataInteracao;
	}

	public Long getId() {
		return id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public TipoInteracao getTipo() {
		return tipo;
	}

	public void setTipo(TipoInteracao tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public LocalDateTime getDataInteracao() {
		return dataInteracao;
	}

	public void setDataInteracao(LocalDateTime dataInteracao) {
		this.dataInteracao = dataInteracao;
	}
}

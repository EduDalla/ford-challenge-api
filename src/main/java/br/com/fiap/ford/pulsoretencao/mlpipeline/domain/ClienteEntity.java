package br.com.fiap.ford.pulsoretencao.mlpipeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class ClienteEntity {

	@Id
	private Long id;

	@Column(name = "nome")
	private String nome;

	@Column(name = "nome_simulado")
	private String nomeSimulado;

	@Column(name = "canal_preferido")
	private String canalPreferido;

	@Column(name = "regiao")
	private String regiao;

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getNomeSimulado() {
		return nomeSimulado;
	}

	public String getCanalPreferido() {
		return canalPreferido;
	}

	public String getRegiao() {
		return regiao;
	}
}

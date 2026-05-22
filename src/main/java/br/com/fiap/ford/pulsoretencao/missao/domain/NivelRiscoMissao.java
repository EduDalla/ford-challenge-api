package br.com.fiap.ford.pulsoretencao.missao.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NivelRiscoMissao {
	BAIXO("baixo"),
	MEDIO("medio"),
	ALTO("alto");

	private final String valor;

	NivelRiscoMissao(String valor) {
		this.valor = valor;
	}

	@JsonValue
	public String getValor() {
		return valor;
	}

	@JsonCreator
	public static NivelRiscoMissao from(String value) {
		for (NivelRiscoMissao risco : values()) {
			if (risco.valor.equalsIgnoreCase(value) || risco.name().equalsIgnoreCase(value)) {
				return risco;
			}
		}
		throw new IllegalArgumentException("Nível de risco inválido: " + value);
	}
}

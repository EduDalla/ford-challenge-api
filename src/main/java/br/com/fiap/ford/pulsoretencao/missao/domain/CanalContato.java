package br.com.fiap.ford.pulsoretencao.missao.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CanalContato {
	WHATSAPP("whatsapp"),
	TELEFONE("telefone"),
	EMAIL("email");

	private final String valor;

	CanalContato(String valor) {
		this.valor = valor;
	}

	@JsonValue
	public String getValor() {
		return valor;
	}

	@JsonCreator
	public static CanalContato from(String value) {
		for (CanalContato canal : values()) {
			if (canal.valor.equalsIgnoreCase(value) || canal.name().equalsIgnoreCase(value)) {
				return canal;
			}
		}
		throw new IllegalArgumentException("Canal de contato inválido: " + value);
	}
}

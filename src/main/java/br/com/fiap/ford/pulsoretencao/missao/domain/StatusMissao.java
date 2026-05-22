package br.com.fiap.ford.pulsoretencao.missao.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusMissao {
	EM_RISCO("em_risco"),
	ASSUMIDO("assumido"),
	CONTATO_FEITO("contato_feito"),
	RESPOSTA_RECEBIDA("resposta_recebida"),
	AGENDADO("agendado"),
	RECUPERADO("recuperado"),
	REPROGRAMAR("reprogramar"),
	PERDIDO("perdido");

	private final String valor;

	StatusMissao(String valor) {
		this.valor = valor;
	}

	@JsonValue
	public String getValor() {
		return valor;
	}

	@JsonCreator
	public static StatusMissao from(String value) {
		for (StatusMissao status : values()) {
			if (status.valor.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Status de missão inválido: " + value);
	}
}

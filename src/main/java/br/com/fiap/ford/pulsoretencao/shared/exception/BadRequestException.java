package br.com.fiap.ford.pulsoretencao.shared.exception;

public class BadRequestException extends RuntimeException {

	public BadRequestException(String message) {
		super(message);
	}
}

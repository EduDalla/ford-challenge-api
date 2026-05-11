package br.com.fiap.ford.pulsoretencao.autenticacao.api;

import jakarta.validation.constraints.NotBlank;

public record DadosAutenticacao(
        @NotBlank
        String login,

        @NotBlank
        String senha
) {
}

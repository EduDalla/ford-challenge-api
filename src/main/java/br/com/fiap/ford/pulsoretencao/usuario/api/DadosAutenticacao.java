package br.com.fiap.ford.pulsoretencao.usuario.api;

import jakarta.validation.constraints.NotBlank;

public record DadosAutenticacao(
        @NotBlank
        String login,

        @NotBlank
        String senha
) {
}

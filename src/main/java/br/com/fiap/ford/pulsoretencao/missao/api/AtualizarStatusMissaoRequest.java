package br.com.fiap.ford.pulsoretencao.missao.api;

import br.com.fiap.ford.pulsoretencao.missao.domain.StatusMissao;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusMissaoRequest(
		@NotNull
		StatusMissao status
) {
}

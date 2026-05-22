package br.com.fiap.ford.pulsoretencao.missao.api;

import br.com.fiap.ford.pulsoretencao.missao.domain.CanalContato;
import br.com.fiap.ford.pulsoretencao.missao.domain.StatusMissao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarAcaoMissaoRequest(
		@NotNull
		StatusMissao tipo,

		@NotNull
		CanalContato canal,

		@Size(max = 500)
		String observacao,

		@Valid
		RegistrarResultadoMissaoRequest resultado
) {
}

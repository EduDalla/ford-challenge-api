package br.com.fiap.ford.pulsoretencao.ml.api;

import java.util.List;

public record MlMissaoResponse(
		String codigoCartao,
		String risco,
		Integer score,
		String prioridadeRadar,
		String perfil,
		String motivoPrincipal,
		String acaoRecomendada,
		List<String> sinaisRadar,
		String modeloVeiculo
) {
}

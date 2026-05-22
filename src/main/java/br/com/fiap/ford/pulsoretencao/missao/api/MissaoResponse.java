package br.com.fiap.ford.pulsoretencao.missao.api;

import java.math.BigDecimal;
import java.util.List;

public record MissaoResponse(
		Long id,
		String codigoCartao,
		MissaoClienteResponse cliente,
		MissaoVeiculoResponse veiculo,
		String perfil,
		String risco,
		Integer score,
		String prioridadeRadar,
		List<String> sinaisRadar,
		String motivoPrincipal,
		String acaoRecomendada,
		String mensagemSugerida,
		BigDecimal valorPotencial,
		BigDecimal impactoVinShareEstimado,
		String prazo,
		String responsavel,
		String status,
		ResultadoMissaoResponse resultado,
		List<HistoricoAcaoResponse> historico
) {
}

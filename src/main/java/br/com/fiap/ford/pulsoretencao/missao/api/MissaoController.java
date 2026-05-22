package br.com.fiap.ford.pulsoretencao.missao.api;

import br.com.fiap.ford.pulsoretencao.missao.service.MissaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MissaoController {

	private final MissaoService missaoService;

	public MissaoController(MissaoService missaoService) {
		this.missaoService = missaoService;
	}

	@GetMapping("/radar/prioridades")
	public List<MissaoResponse> radar(Authentication authentication) {
		return missaoService.radar(authentication);
	}

	@GetMapping("/missoes")
	public List<MissaoResponse> listar(Authentication authentication) {
		return missaoService.listar(authentication);
	}

	@PostMapping("/missoes")
	public ResponseEntity<MissaoResponse> criar(@RequestBody @Valid CriarMissaoRequest request,
			Authentication authentication) {
		return ResponseEntity.status(HttpStatus.CREATED).body(missaoService.criar(request, authentication));
	}

	@GetMapping("/missoes/{id}")
	public MissaoResponse buscar(@PathVariable Long id, Authentication authentication) {
		return missaoService.buscarPorId(id, authentication);
	}

	@GetMapping("/cartoes/{codigoCartao}")
	public MissaoResponse buscarCartao(@PathVariable String codigoCartao, Authentication authentication) {
		return missaoService.buscarPorCodigoCartao(codigoCartao, authentication);
	}

	@PatchMapping("/missoes/{id}/status")
	public MissaoResponse atualizarStatus(@PathVariable Long id,
			@RequestBody @Valid AtualizarStatusMissaoRequest request,
			Authentication authentication) {
		return missaoService.atualizarStatus(id, request, authentication);
	}

	@PostMapping("/missoes/{id}/acoes")
	public MissaoResponse registrarAcao(@PathVariable Long id,
			@RequestBody @Valid RegistrarAcaoMissaoRequest request,
			Authentication authentication) {
		return missaoService.registrarAcao(id, request, authentication);
	}

	@PostMapping("/missoes/{id}/resultado")
	public MissaoResponse registrarResultado(@PathVariable Long id,
			@RequestBody @Valid RegistrarResultadoMissaoRequest request,
			Authentication authentication) {
		return missaoService.registrarResultado(id, request, authentication);
	}

	@GetMapping("/indicadores/consultor")
	public IndicadoresRetencaoResponse indicadoresConsultor(Authentication authentication) {
		return missaoService.indicadoresConsultor(authentication);
	}

	@GetMapping("/indicadores/retencao")
	public IndicadoresRetencaoResponse indicadoresRetencao(Authentication authentication) {
		return missaoService.indicadoresRetencao(authentication);
	}
}

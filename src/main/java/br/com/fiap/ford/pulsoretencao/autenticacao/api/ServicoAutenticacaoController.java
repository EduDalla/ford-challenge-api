package br.com.fiap.ford.pulsoretencao.autenticacao.api;

import br.com.fiap.ford.pulsoretencao.autenticacao.service.AuthenticationRateLimiter;
import br.com.fiap.ford.pulsoretencao.infra.security.TokenService;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import br.com.fiap.ford.pulsoretencao.integracao.service.ApiClientService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ServicoAutenticacaoController {

	private final ApiClientService apiClientService;
	private final TokenService tokenService;
	private final AuthenticationRateLimiter rateLimiter;

	public ServicoAutenticacaoController(ApiClientService apiClientService, TokenService tokenService,
			AuthenticationRateLimiter rateLimiter) {
		this.apiClientService = apiClientService;
		this.tokenService = tokenService;
		this.rateLimiter = rateLimiter;
	}

	@PostMapping("/service-token")
	@Operation(summary = "Autenticação de cliente técnico", security = {})
	public ResponseEntity<DadosTokenJWT> gerarTokenServico(@RequestBody @Valid DadosAutenticacaoServico dados,
			HttpServletRequest request) {
		String rateLimitKey = rateLimiter.key(dados.clientId(), request.getRemoteAddr());
		if (!rateLimiter.isAllowed(rateLimitKey)) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
		}

		try {
			ApiClient apiClient = apiClientService.autenticar(dados);
			rateLimiter.reset(rateLimitKey);
			return ResponseEntity.ok(tokenService.gerarTokenServico(apiClient));
		} catch (AuthenticationException exception) {
			rateLimiter.recordFailure(rateLimitKey);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}

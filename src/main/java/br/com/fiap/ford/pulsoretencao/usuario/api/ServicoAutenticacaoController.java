package br.com.fiap.ford.pulsoretencao.usuario.api;

import br.com.fiap.ford.pulsoretencao.infra.security.TokenService;
import br.com.fiap.ford.pulsoretencao.usuario.ApiClient;
import br.com.fiap.ford.pulsoretencao.usuario.ApiClientService;
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

	public ServicoAutenticacaoController(ApiClientService apiClientService, TokenService tokenService) {
		this.apiClientService = apiClientService;
		this.tokenService = tokenService;
	}

	@PostMapping("/service-token")
	public ResponseEntity<DadosTokenJWT> gerarTokenServico(@RequestBody @Valid DadosAutenticacaoServico dados) {
		try {
			ApiClient apiClient = apiClientService.autenticar(dados);
			return ResponseEntity.ok(tokenService.gerarTokenServico(apiClient));
		} catch (AuthenticationException exception) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}

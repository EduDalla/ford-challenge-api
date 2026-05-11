package br.com.fiap.ford.pulsoretencao.usuario.api;

import br.com.fiap.ford.pulsoretencao.usuario.api.DadosTokenJWT;
import br.com.fiap.ford.pulsoretencao.infra.security.TokenService;
import br.com.fiap.ford.pulsoretencao.usuario.Usuario;
import br.com.fiap.ford.pulsoretencao.usuario.UsuarioAutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final TokenService tokenService;
    private final UsuarioAutenticacaoService autenticacaoService;

    public AutenticacaoController(AuthenticationManager manager, TokenService tokenService,
            UsuarioAutenticacaoService autenticacaoService) {
        this.manager = manager;
        this.tokenService = tokenService;
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());

        try {
            var authentication = manager.authenticate(authenticationToken);
            autenticacaoService.registrarLoginComSucesso(dados.login());

            var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());
            return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
        } catch (AuthenticationException exception) {
            autenticacaoService.registrarFalhaLogin(dados.login());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}

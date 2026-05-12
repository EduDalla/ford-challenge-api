package br.com.fiap.ford.pulsoretencao.infra.security;

import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClientPermission;
import br.com.fiap.ford.pulsoretencao.integracao.domain.Permission;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import br.com.fiap.ford.pulsoretencao.usuario.domain.Usuario;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import br.com.fiap.ford.pulsoretencao.autenticacao.api.DadosTokenJWT;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TokenService {

    private final String secret;

    public TokenService(@Value("${api.security.token.secret:${JWT_SECRET:dev-secret-pulso-retencao}}") String secret) {
        this.secret = secret;
    }

    public DadosTokenJWT gerarToken(Usuario usuario) {
        Instant expiraEm = dataExpiracao();
        try {
            var algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("API Ford")
                    .withSubject(usuario.getLogin())
                    .withClaim("type", "user")
                    .withClaim("role", usuario.getPerfil().name())
                    .withExpiresAt(expiraEm)
                    .sign(algorithm);
            return new DadosTokenJWT(token, "user", expiraEm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar o token", exception);
        }
    }

    public DadosTokenJWT gerarTokenServico(ApiClient apiClient) {
        Instant expiraEm = dataExpiracao();
        List<String> scopes = apiClient.getPermissions()
                .stream()
                .map(ApiClientPermission::getPermission)
                .filter(permission -> Boolean.TRUE.equals(permission.getAtivo()))
                .map(Permission::getCodigo)
                .toList();

        try {
            var algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("API Ford")
                    .withSubject(apiClient.getClientId())
                    .withClaim("type", "service")
                    .withClaim("scopes", scopes)
                    .withExpiresAt(expiraEm)
                    .sign(algorithm);
            return new DadosTokenJWT(token, "service", expiraEm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar o token de serviço", exception);
        }
    }

    public DadosTokenAutenticado validarToken(String token) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("API Ford")
                    .build()
                    .verify(token);

            return new DadosTokenAutenticado(
                    jwt.getSubject(),
                    jwt.getClaim("type").asString(),
                    jwt.getClaim("role").asString(),
                    jwt.getClaim("scopes").asList(String.class)
            );
        } catch (JWTVerificationException exception) {
            throw new BadCredentialsJwtException("Token JWT inválido.", exception);
        }
    }

    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    public static class BadCredentialsJwtException extends RuntimeException {

        public BadCredentialsJwtException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

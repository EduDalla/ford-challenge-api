package br.com.fiap.ford.pulsoretencao.infra.security;

import br.com.fiap.ford.pulsoretencao.autenticacao.api.DadosTokenJWT;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClientPermission;
import br.com.fiap.ford.pulsoretencao.integracao.domain.Permission;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TokenService {

    private static final String SERVICE_TOKEN_ISSUER = "API Ford";
    private static final String SERVICE_TOKEN_TYPE = "service";
    private static final String USER_TOKEN_TYPE = "user";

    private final String serviceSecret;
    private final String supabaseJwtSecret;
    private final String supabaseJwtIssuer;
    private final String supabaseJwtAudience;

    public TokenService(
            @Value("${api.security.token.secret:${JWT_SECRET:dev-secret-pulso-retencao}}") String serviceSecret,
            @Value("${supabase.jwt.secret:${SUPABASE_JWT_SECRET:}}") String supabaseJwtSecret,
            @Value("${supabase.jwt.issuer:${SUPABASE_JWT_ISSUER:}}") String supabaseJwtIssuer,
            @Value("${supabase.jwt.audience:${SUPABASE_JWT_AUDIENCE:authenticated}}") String supabaseJwtAudience) {
        this.serviceSecret = serviceSecret;
        this.supabaseJwtSecret = supabaseJwtSecret;
        this.supabaseJwtIssuer = supabaseJwtIssuer;
        this.supabaseJwtAudience = supabaseJwtAudience;
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
            var algorithm = Algorithm.HMAC256(serviceSecret);
            String token = JWT.create()
                    .withIssuer(SERVICE_TOKEN_ISSUER)
                    .withSubject(apiClient.getClientId())
                    .withClaim("type", SERVICE_TOKEN_TYPE)
                    .withClaim("scopes", scopes)
                    .withExpiresAt(expiraEm)
                    .sign(algorithm);
            return new DadosTokenJWT(token, SERVICE_TOKEN_TYPE, expiraEm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar o token de servico", exception);
        }
    }

    public DadosTokenAutenticado validarToken(String token) {
        try {
            return validarTokenServico(token);
        } catch (BadCredentialsJwtException exception) {
            return validarTokenSupabase(token);
        }
    }

    private DadosTokenAutenticado validarTokenServico(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(serviceSecret))
                    .withIssuer(SERVICE_TOKEN_ISSUER)
                    .build()
                    .verify(token);

            return new DadosTokenAutenticado(
                    jwt.getSubject(),
                    jwt.getClaim("type").asString(),
                    jwt.getClaim("role").asString(),
                    jwt.getClaim("scopes").asList(String.class)
            );
        } catch (JWTVerificationException exception) {
            throw new BadCredentialsJwtException("Token de servico invalido.", exception);
        }
    }

    private DadosTokenAutenticado validarTokenSupabase(String token) {
        if (supabaseJwtSecret == null || supabaseJwtSecret.isBlank()) {
            throw new BadCredentialsJwtException("SUPABASE_JWT_SECRET nao configurado.", null);
        }

        try {
            Verification verification = JWT.require(Algorithm.HMAC256(supabaseJwtSecret));
            if (supabaseJwtIssuer != null && !supabaseJwtIssuer.isBlank()) {
                verification.withIssuer(supabaseJwtIssuer);
            }
            if (supabaseJwtAudience != null && !supabaseJwtAudience.isBlank()) {
                verification.withAudience(supabaseJwtAudience);
            }

            DecodedJWT jwt = verification.build().verify(token);
            return new DadosTokenAutenticado(
                    jwt.getSubject(),
                    USER_TOKEN_TYPE,
                    null,
                    List.of()
            );
        } catch (JWTVerificationException exception) {
            throw new BadCredentialsJwtException("Token Supabase invalido.", exception);
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

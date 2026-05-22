package br.com.fiap.ford.pulsoretencao.infra.security;

import br.com.fiap.ford.pulsoretencao.autenticacao.api.DadosTokenJWT;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClientPermission;
import br.com.fiap.ford.pulsoretencao.integracao.domain.Permission;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private static final String SERVICE_TOKEN_ISSUER = "API Ford";
    private static final String SERVICE_TOKEN_TYPE = "service";
    private static final String USER_TOKEN_TYPE = "user";

    private final String serviceSecret;
    private final String supabaseJwtSecret;
    private final String supabaseJwksUrl;
    private final String supabaseJwtIssuer;
    private final String supabaseJwtAudience;
    private final JwkProvider supabaseJwkProvider;

    public TokenService(
            @Value("${api.security.token.secret:dev-secret-pulso-retencao}") String serviceSecret,
            @Value("${supabase.jwt.secret:}") String supabaseJwtSecret,
            @Value("${supabase.jwks-url:}") String supabaseJwksUrl,
            @Value("${supabase.jwt.issuer:}") String supabaseJwtIssuer,
            @Value("${supabase.jwt.audience:authenticated}") String supabaseJwtAudience) {
        this.serviceSecret = serviceSecret;
        this.supabaseJwtSecret = supabaseJwtSecret;
        this.supabaseJwksUrl = supabaseJwksUrl;
        this.supabaseJwtIssuer = supabaseJwtIssuer;
        this.supabaseJwtAudience = supabaseJwtAudience;
        this.supabaseJwkProvider = criarJwkProvider(supabaseJwksUrl);
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
        BadCredentialsJwtException jwksException = null;

        if (supabaseJwkProvider != null) {
            try {
                return validarTokenSupabaseJwks(token);
            } catch (BadCredentialsJwtException exception) {
                jwksException = exception;
            }
        }

        if (hasText(supabaseJwtSecret)) {
            return validarTokenSupabaseLegacy(token);
        }

        if (jwksException != null) {
            throw jwksException;
        }

        throw new BadCredentialsJwtException("SUPABASE_JWKS_URL ou SUPABASE_JWT_SECRET nao configurado.", null);
    }

    private DadosTokenAutenticado validarTokenSupabaseLegacy(String token) {
        try {
            Verification verification = supabaseVerification(Algorithm.HMAC256(supabaseJwtSecret));
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

    private DadosTokenAutenticado validarTokenSupabaseJwks(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String keyId = decodedJWT.getKeyId();
            if (!hasText(keyId)) {
                throw new BadCredentialsJwtException("Token Supabase sem kid.", null);
            }

            Jwk jwk = supabaseJwkProvider.get(keyId);
            Algorithm algorithm = algorithmFromJwk(decodedJWT.getAlgorithm(), jwk);
            DecodedJWT jwt = supabaseVerification(algorithm).build().verify(token);
            return new DadosTokenAutenticado(
                    jwt.getSubject(),
                    USER_TOKEN_TYPE,
                    null,
                    List.of()
            );
        } catch (JwkException | JWTVerificationException | IllegalArgumentException exception) {
            throw new BadCredentialsJwtException("Token Supabase invalido via JWKS.", exception);
        }
    }

    private Verification supabaseVerification(Algorithm algorithm) {
        Verification verification = JWT.require(algorithm);
        if (hasText(supabaseJwtIssuer)) {
            verification.withIssuer(supabaseJwtIssuer);
        }
        if (hasText(supabaseJwtAudience)) {
            verification.withAudience(supabaseJwtAudience);
        }
        return verification;
    }

    private Algorithm algorithmFromJwk(String jwtAlgorithm, Jwk jwk) throws JwkException {
        return switch (jwtAlgorithm) {
            case "ES256" -> Algorithm.ECDSA256(toEcPublicKey(jwk), null);
            case "ES384" -> Algorithm.ECDSA384(toEcPublicKey(jwk), null);
            case "ES512" -> Algorithm.ECDSA512(toEcPublicKey(jwk), null);
            case "RS256" -> Algorithm.RSA256(toRsaPublicKey(jwk), null);
            case "RS384" -> Algorithm.RSA384(toRsaPublicKey(jwk), null);
            case "RS512" -> Algorithm.RSA512(toRsaPublicKey(jwk), null);
            default -> throw new IllegalArgumentException("Algoritmo Supabase nao suportado: " + jwtAlgorithm);
        };
    }

    private ECPublicKey toEcPublicKey(Jwk jwk) throws JwkException {
        if (jwk.getPublicKey() instanceof ECPublicKey publicKey) {
            return publicKey;
        }
        throw new IllegalArgumentException("JWKS retornou chave nao EC para token ES.");
    }

    private RSAPublicKey toRsaPublicKey(Jwk jwk) throws JwkException {
        if (jwk.getPublicKey() instanceof RSAPublicKey publicKey) {
            return publicKey;
        }
        throw new IllegalArgumentException("JWKS retornou chave nao RSA para token RS.");
    }

    private JwkProvider criarJwkProvider(String jwksUrl) {
        if (!hasText(jwksUrl)) {
            return null;
        }

        try {
            return new JwkProviderBuilder(new URI(jwksUrl.trim()).toURL())
                    .cached(10, 10, TimeUnit.MINUTES)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build();
        } catch (MalformedURLException | URISyntaxException exception) {
            throw new IllegalArgumentException("SUPABASE_JWKS_URL invalida.", exception);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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

package br.com.fiap.ford.pulsoretencao.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    private static final Set<String> ML_AUTHORITIES = Set.of(
            "SCOPE_ml:predict",
            "ROLE_ADMIN",
            "ROLE_ANALISTA"
    );

    private final boolean demoMode;
    private final String mlDemoToken;
    private final Environment environment;

    public SecurityConfigurations(
            @Value("${app.demo-mode:false}") boolean demoMode,
            @Value("${app.ml.demo-token:}") String mlDemoToken,
            Environment environment
    ) {
        this.demoMode = demoMode;
        this.mlDemoToken = mlDemoToken;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityFilter securityFilter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/health",
                                "/actuator/health",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/ml/**", "/api/v1/ml/**")
                        .access((authentication, context) ->
                                new AuthorizationDecision(podeAcessarMl(authentication.get(), context.getRequest())))
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:8081}") String allowedOrigins
    ) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        if (environment.acceptsProfiles(Profiles.of("prod")) && origins.stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS nao pode ser wildcard no profile prod.");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private boolean podeAcessarMl(Authentication authentication, HttpServletRequest request) {
        return headerDemoValido(request) || tokenComPermissaoMl(authentication);
    }

    private boolean tokenComPermissaoMl(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ML_AUTHORITIES::contains);
    }

    private boolean headerDemoValido(HttpServletRequest request) {
        if (!demoMode || mlDemoToken == null || mlDemoToken.isBlank()) {
            return false;
        }

        String tokenInformado = request.getHeader("X-ML-Demo-Token");
        if (tokenInformado == null || tokenInformado.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                tokenInformado.trim().getBytes(StandardCharsets.UTF_8),
                mlDemoToken.trim().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

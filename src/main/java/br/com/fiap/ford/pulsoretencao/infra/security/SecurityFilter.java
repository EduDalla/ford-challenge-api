package br.com.fiap.ford.pulsoretencao.infra.security;

import br.com.fiap.ford.pulsoretencao.profile.domain.Profile;
import br.com.fiap.ford.pulsoretencao.profile.repository.ProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final ProfileRepository profileRepository;
    private final Environment environment;

    public SecurityFilter(TokenService tokenService, ProfileRepository profileRepository, Environment environment) {
        this.tokenService = tokenService;
        this.profileRepository = profileRepository;
        this.environment = environment;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();
        return path.equals("/")
                || path.equals("/health")
                || path.equals("/actuator/health")
                || path.equals("/healthCheck")
                || isPublicDocsPath(path);
    }

    private boolean isPublicDocsPath(String path) {
        return !environment.acceptsProfiles(Profiles.of("prod"))
                && (path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = recuperarToken(request);

        if (token != null) {
            try {
                DadosTokenAutenticado dados = tokenService.validarToken(token);
                var authentication = criarAuthentication(dados);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException exception) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken criarAuthentication(DadosTokenAutenticado dados) {
        if ("user".equals(dados.type())) {
            Profile profile = buscarProfileAtivo(dados.subject());
            return new UsernamePasswordAuthenticationToken(
                    profile,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + profile.getPerfil().name()))
            );
        }

        return new UsernamePasswordAuthenticationToken(
                dados.subject(),
                null,
                toServiceAuthorities(dados)
        );
    }

    private Profile buscarProfileAtivo(String subject) {
        UUID profileId = UUID.fromString(subject);
        return profileRepository.findByIdAndAtivoTrue(profileId)
                .orElseThrow(() -> new TokenService.BadCredentialsJwtException("Profile Supabase inativo ou nao encontrado.", null));
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7).trim();
    }

    private List<SimpleGrantedAuthority> toServiceAuthorities(DadosTokenAutenticado dados) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if ("service".equals(dados.type()) && dados.scopes() != null) {
            dados.scopes().forEach(scope -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope)));
        }
        return authorities;
    }
}

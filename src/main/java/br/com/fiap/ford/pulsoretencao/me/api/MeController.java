package br.com.fiap.ford.pulsoretencao.me.api;

import br.com.fiap.ford.pulsoretencao.profile.domain.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

	@GetMapping
	public MeResponse me(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Profile profile)) {
			throw new AccessDeniedException("Endpoint disponivel apenas para usuarios Supabase.");
		}
		return new MeResponse(profile.getId(), profile.getNome(), profile.getEmail(), profile.getPerfil().name());
	}
}

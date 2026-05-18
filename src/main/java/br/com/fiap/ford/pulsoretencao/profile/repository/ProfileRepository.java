package br.com.fiap.ford.pulsoretencao.profile.repository;

import br.com.fiap.ford.pulsoretencao.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

	Optional<Profile> findByIdAndAtivoTrue(UUID id);
}

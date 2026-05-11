package br.com.fiap.ford.pulsoretencao.integracao.repository;

import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {

	@EntityGraph(attributePaths = "permissions.permission")
	Optional<ApiClient> findByClientIdAndAtivoTrue(String clientId);
}

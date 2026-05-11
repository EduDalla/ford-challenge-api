package br.com.fiap.ford.pulsoretencao.integracao.repository;

import br.com.fiap.ford.pulsoretencao.integracao.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

	Optional<Permission> findByCodigoAndAtivoTrue(String codigo);
}

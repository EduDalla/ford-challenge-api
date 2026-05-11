package br.com.fiap.ford.pulsoretencao.integracao.repository;

import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClientPermission;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClientPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiClientPermissionRepository extends JpaRepository<ApiClientPermission, ApiClientPermissionId> {
}

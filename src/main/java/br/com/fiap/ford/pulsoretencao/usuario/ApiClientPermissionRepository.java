package br.com.fiap.ford.pulsoretencao.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiClientPermissionRepository extends JpaRepository<ApiClientPermission, ApiClientPermissionId> {
}

package br.com.fiap.ford.pulsoretencao.usuario;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "api_client_permissions")
public class ApiClientPermission {

	@EmbeddedId
	private ApiClientPermissionId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("apiClientId")
	@JoinColumn(name = "api_client_id", nullable = false)
	private ApiClient apiClient;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@MapsId("permissionId")
	@JoinColumn(name = "permission_id", nullable = false)
	private Permission permission;

	public ApiClientPermission() {
	}

	public Permission getPermission() {
		return permission;
	}
}

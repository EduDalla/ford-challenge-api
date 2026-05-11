package br.com.fiap.ford.pulsoretencao.integracao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ApiClientPermissionId implements Serializable {

	@Column(name = "api_client_id")
	private Long apiClientId;

	@Column(name = "permission_id")
	private Long permissionId;

	public ApiClientPermissionId() {
	}

	public ApiClientPermissionId(Long apiClientId, Long permissionId) {
		this.apiClientId = apiClientId;
		this.permissionId = permissionId;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ApiClientPermissionId that)) {
			return false;
		}
		return Objects.equals(apiClientId, that.apiClientId)
				&& Objects.equals(permissionId, that.permissionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiClientId, permissionId);
	}
}

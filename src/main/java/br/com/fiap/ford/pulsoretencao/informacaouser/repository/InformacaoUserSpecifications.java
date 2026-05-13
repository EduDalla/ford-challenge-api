package br.com.fiap.ford.pulsoretencao.informacaouser.repository;

import br.com.fiap.ford.pulsoretencao.informacaouser.domain.InformacaoUser;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class InformacaoUserSpecifications {

	private InformacaoUserSpecifications() {
	}

	public static Specification<InformacaoUser> userId(Long userId) {
		return (root, query, criteriaBuilder) -> userId == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("usuario").get("id"), userId);
	}

	public static Specification<InformacaoUser> informacaoId(Long informacaoId) {
		return (root, query, criteriaBuilder) -> informacaoId == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("informacao").get("id"), informacaoId);
	}

	public static Specification<InformacaoUser> dataAlerta(LocalDate dataAlerta) {
		return (root, query, criteriaBuilder) -> dataAlerta == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("dataAlerta"), dataAlerta);
	}
}

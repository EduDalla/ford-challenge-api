package br.com.fiap.ford.pulsoretencao.cliente.repository;

import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import br.com.fiap.ford.pulsoretencao.cliente.domain.NivelRisco;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ClienteSpecifications {

	private ClienteSpecifications() {
	}

	public static Specification<Cliente> naoExcluido() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("excluidoEm"));
	}

	public static Specification<Cliente> texto(String termo) {
		return (root, query, criteriaBuilder) -> {
			if (!StringUtils.hasText(termo)) {
				return criteriaBuilder.conjunction();
			}

			String like = "%" + termo.trim().toLowerCase() + "%";
			return criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), like),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), like),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("documento")), like),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("telefone")), like),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("segmento")), like)
			);
		};
	}

	public static Specification<Cliente> nivelRisco(NivelRisco nivelRisco) {
		return (root, query, criteriaBuilder) -> nivelRisco == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("nivelRisco"), nivelRisco);
	}

	public static Specification<Cliente> ativo(Boolean ativo) {
		return (root, query, criteriaBuilder) -> ativo == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("ativo"), ativo);
	}
}

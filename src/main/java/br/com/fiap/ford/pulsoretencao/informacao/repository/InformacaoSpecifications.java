package br.com.fiap.ford.pulsoretencao.informacao.repository;

import br.com.fiap.ford.pulsoretencao.informacao.domain.Informacao;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class InformacaoSpecifications {

	private InformacaoSpecifications() {
	}

	public static Specification<Informacao> texto(String termo) {
		return (root, query, criteriaBuilder) -> {
			if (!StringUtils.hasText(termo)) {
				return criteriaBuilder.conjunction();
			}

			String like = "%" + termo.trim().toLowerCase() + "%";
			return criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), like),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), like)
			);
		};
	}

	public static Specification<Informacao> ativo(Boolean ativo) {
		return (root, query, criteriaBuilder) -> ativo == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("ativo"), ativo);
	}
}

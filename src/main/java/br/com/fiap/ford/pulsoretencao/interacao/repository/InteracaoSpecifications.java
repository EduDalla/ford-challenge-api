package br.com.fiap.ford.pulsoretencao.interacao.repository;

import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import br.com.fiap.ford.pulsoretencao.interacao.domain.Interacao;
import br.com.fiap.ford.pulsoretencao.interacao.domain.TipoInteracao;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class InteracaoSpecifications {

	private InteracaoSpecifications() {
	}

	public static Specification<Interacao> naoExcluida() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("excluidoEm"));
	}

	public static Specification<Interacao> clienteNaoExcluido() {
		return (root, query, criteriaBuilder) -> {
			Join<Interacao, Cliente> cliente = root.join("cliente");
			return criteriaBuilder.isNull(cliente.get("excluidoEm"));
		};
	}

	public static Specification<Interacao> clienteId(Long clienteId) {
		return (root, query, criteriaBuilder) -> {
			Join<Interacao, Cliente> cliente = root.join("cliente");
			return criteriaBuilder.equal(cliente.get("id"), clienteId);
		};
	}

	public static Specification<Interacao> texto(String termo) {
		return (root, query, criteriaBuilder) -> {
			if (!StringUtils.hasText(termo)) {
				return criteriaBuilder.conjunction();
			}

			String like = "%" + termo.trim().toLowerCase() + "%";
			return criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.lower(root.get("descricao")), like),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("resultado")), like)
			);
		};
	}

	public static Specification<Interacao> tipo(TipoInteracao tipo) {
		return (root, query, criteriaBuilder) -> tipo == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("tipo"), tipo);
	}

	public static Specification<Interacao> dataInteracaoMaiorOuIgual(LocalDateTime dataInicio) {
		return (root, query, criteriaBuilder) -> dataInicio == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.greaterThanOrEqualTo(root.get("dataInteracao"), dataInicio);
	}

	public static Specification<Interacao> dataInteracaoMenorOuIgual(LocalDateTime dataFim) {
		return (root, query, criteriaBuilder) -> dataFim == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.lessThanOrEqualTo(root.get("dataInteracao"), dataFim);
	}
}

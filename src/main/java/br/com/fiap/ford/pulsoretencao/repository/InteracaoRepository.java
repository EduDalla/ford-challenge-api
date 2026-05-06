package br.com.fiap.ford.pulsoretencao.repository;

import br.com.fiap.ford.pulsoretencao.model.Interacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteracaoRepository extends JpaRepository<Interacao, Long> {

	List<Interacao> findByClienteIdOrderByDataInteracaoDesc(Long clienteId);

	long countByClienteId(Long clienteId);
}

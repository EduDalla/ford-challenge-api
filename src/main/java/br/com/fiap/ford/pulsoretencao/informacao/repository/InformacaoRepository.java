package br.com.fiap.ford.pulsoretencao.informacao.repository;

import br.com.fiap.ford.pulsoretencao.informacao.domain.Informacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InformacaoRepository extends JpaRepository<Informacao, Long>, JpaSpecificationExecutor<Informacao> {

	Optional<Informacao> findByIdAndAtivoTrue(Long id);

	boolean existsByNome(String nome);

	boolean existsByNomeAndIdNot(String nome, Long id);
}

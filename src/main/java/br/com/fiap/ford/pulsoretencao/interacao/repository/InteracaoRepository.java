package br.com.fiap.ford.pulsoretencao.interacao.repository;

import br.com.fiap.ford.pulsoretencao.interacao.domain.Interacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InteracaoRepository extends JpaRepository<Interacao, Long>, JpaSpecificationExecutor<Interacao> {

	@Query("""
			SELECT i
			FROM Interacao i
			JOIN i.cliente c
			WHERE i.id = :id
			  AND i.excluidoEm IS NULL
			  AND c.excluidoEm IS NULL
			""")
	Optional<Interacao> findAtivaById(@Param("id") Long id);

	@Modifying
	@Query("""
			UPDATE Interacao i
			SET i.excluidoEm = :dataExclusao
			WHERE i.cliente.id = :clienteId
			  AND i.excluidoEm IS NULL
			""")
	void softDeleteByClienteId(@Param("clienteId") Long clienteId,
			@Param("dataExclusao") LocalDateTime dataExclusao);
}

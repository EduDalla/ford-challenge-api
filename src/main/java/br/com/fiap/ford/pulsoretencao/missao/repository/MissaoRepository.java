package br.com.fiap.ford.pulsoretencao.missao.repository;

import br.com.fiap.ford.pulsoretencao.missao.domain.Missao;
import br.com.fiap.ford.pulsoretencao.missao.domain.StatusMissao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MissaoRepository extends JpaRepository<Missao, Long> {

	boolean existsByCodigoCartaoIgnoreCase(String codigoCartao);

	@EntityGraph(attributePaths = {"cliente", "veiculo", "responsavel", "resultado", "historico"})
	Optional<Missao> findByCodigoCartaoIgnoreCase(String codigoCartao);

	@EntityGraph(attributePaths = {"cliente", "veiculo", "responsavel", "resultado", "historico"})
	Optional<Missao> findById(Long id);

	@EntityGraph(attributePaths = {"cliente", "veiculo", "responsavel", "resultado", "historico"})
	@Query("""
			select distinct m from Missao m
			where m.responsavel is null or m.responsavel.id = :profileId
			order by m.prioridadeRadar asc, m.score desc, m.criadoEm asc
			""")
	List<Missao> findVisibleToAnalista(@Param("profileId") UUID profileId);

	@EntityGraph(attributePaths = {"cliente", "veiculo", "responsavel", "resultado", "historico"})
	@Query("""
			select distinct m from Missao m
			where m.status in :status
			order by m.prioridadeRadar asc, m.score desc, m.criadoEm asc
			""")
	List<Missao> findAllByStatusInOrdered(@Param("status") Collection<StatusMissao> status);

	@EntityGraph(attributePaths = {"cliente", "veiculo", "responsavel", "resultado", "historico"})
	@Query("""
			select distinct m from Missao m
			where (m.responsavel is null or m.responsavel.id = :profileId)
			  and m.status in :status
			order by m.prioridadeRadar asc, m.score desc, m.criadoEm asc
			""")
	List<Missao> findVisibleByStatusIn(@Param("profileId") UUID profileId, @Param("status") Collection<StatusMissao> status);

	@EntityGraph(attributePaths = {"cliente", "veiculo", "responsavel", "resultado", "historico"})
	@Query("select distinct m from Missao m order by m.prioridadeRadar asc, m.score desc, m.criadoEm asc")
	List<Missao> findAllOrdered();
}

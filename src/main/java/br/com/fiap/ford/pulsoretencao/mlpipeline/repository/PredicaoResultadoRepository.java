package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.PredicaoResultadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("mlPipelinePredicaoResultadoRepository")
public interface PredicaoResultadoRepository extends JpaRepository<PredicaoResultadoEntity, Long> {
}

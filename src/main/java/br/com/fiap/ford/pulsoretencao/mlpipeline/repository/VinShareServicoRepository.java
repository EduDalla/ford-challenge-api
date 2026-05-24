package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.VinShareServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("mlPipelineVinShareServicoRepository")
public interface VinShareServicoRepository extends JpaRepository<VinShareServicoEntity, Long> {
}

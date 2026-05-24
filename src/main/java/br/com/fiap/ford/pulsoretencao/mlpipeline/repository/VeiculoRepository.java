package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("mlPipelineVeiculoRepository")
public interface VeiculoRepository extends JpaRepository<VeiculoEntity, Long> {

	Optional<VeiculoEntity> findByVinHash(String vinHash);

	Optional<VeiculoEntity> findByVinSimulado(String vinSimulado);
}

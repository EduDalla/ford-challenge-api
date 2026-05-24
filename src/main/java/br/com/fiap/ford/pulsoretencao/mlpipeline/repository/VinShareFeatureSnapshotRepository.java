package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.VinShareFeatureSnapshotEntity;
import br.com.fiap.ford.pulsoretencao.mlpipeline.enums.FeatureSnapshotPredictionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("mlPipelineVinShareFeatureSnapshotRepository")
public interface VinShareFeatureSnapshotRepository
		extends JpaRepository<VinShareFeatureSnapshotEntity, Long>, VinShareFeatureSnapshotRepositoryCustom {

	long countByStatusPredicao(FeatureSnapshotPredictionStatus status);
}

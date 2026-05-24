package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.VinShareFeatureSnapshotEntity;

import java.util.List;

public interface VinShareFeatureSnapshotRepositoryCustom {

	List<VinShareFeatureSnapshotEntity> reservePendingForPrediction(int limit, int retryDelayMinutes);
}

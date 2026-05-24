package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.MlFeatureRefreshQueueEntity;
import br.com.fiap.ford.pulsoretencao.mlpipeline.enums.MlFeatureRefreshQueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("mlPipelineMlFeatureRefreshQueueRepository")
public interface MlFeatureRefreshQueueRepository extends JpaRepository<MlFeatureRefreshQueueEntity, Long> {

	List<MlFeatureRefreshQueueEntity> findTop100ByStatusAndDataCorteOrderByCriadoEmAsc(
			MlFeatureRefreshQueueStatus status,
			LocalDate dataCorte
	);
}

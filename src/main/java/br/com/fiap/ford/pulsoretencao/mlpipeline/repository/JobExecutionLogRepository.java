package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.JobExecutionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("mlPipelineJobExecutionLogRepository")
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLogEntity, UUID> {
}

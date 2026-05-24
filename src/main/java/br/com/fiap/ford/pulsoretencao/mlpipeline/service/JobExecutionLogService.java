package br.com.fiap.ford.pulsoretencao.mlpipeline.service;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.JobExecutionLogEntity;
import br.com.fiap.ford.pulsoretencao.mlpipeline.repository.JobExecutionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobExecutionLogService {

	private final JobExecutionLogRepository repository;

	public JobExecutionLogService(JobExecutionLogRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public JobExecutionLogEntity start(String jobName) {
		return repository.save(JobExecutionLogEntity.start(jobName));
	}

	@Transactional
	public void markSuccess(JobExecutionLogEntity log, int totalProcessed, int totalSuccess, int totalFailed) {
		log.finishSuccess(totalProcessed, totalSuccess, totalFailed);
		repository.save(log);
	}

	@Transactional
	public void markSkipped(JobExecutionLogEntity log, String message) {
		log.finishSkipped(message);
		repository.save(log);
	}

	@Transactional
	public void markFailed(JobExecutionLogEntity log, int totalProcessed, int totalSuccess, int totalFailed, String message) {
		log.finishFailed(totalProcessed, totalSuccess, totalFailed, message);
		repository.save(log);
	}
}

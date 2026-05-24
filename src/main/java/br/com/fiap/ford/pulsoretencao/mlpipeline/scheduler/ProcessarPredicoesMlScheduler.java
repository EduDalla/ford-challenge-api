package br.com.fiap.ford.pulsoretencao.mlpipeline.scheduler;

import br.com.fiap.ford.pulsoretencao.mlpipeline.config.ProcessarPredicoesMlProperties;
import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.JobExecutionLogEntity;
import br.com.fiap.ford.pulsoretencao.mlpipeline.dto.ProcessarPredicoesResult;
import br.com.fiap.ford.pulsoretencao.mlpipeline.service.JobExecutionLogService;
import br.com.fiap.ford.pulsoretencao.mlpipeline.service.ProcessarPredicoesMlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProcessarPredicoesMlScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessarPredicoesMlScheduler.class);
	private static final String JOB_NAME = "ProcessarPredicoesMlScheduler";

	private final ProcessarPredicoesMlService service;
	private final ProcessarPredicoesMlProperties properties;
	private final JobExecutionLogService jobExecutionLogService;

	public ProcessarPredicoesMlScheduler(
			ProcessarPredicoesMlService service,
			ProcessarPredicoesMlProperties properties,
			JobExecutionLogService jobExecutionLogService) {
		this.service = service;
		this.properties = properties;
		this.jobExecutionLogService = jobExecutionLogService;
	}

	@Scheduled(cron = "${app.jobs.processar-predicoes-ml.cron:0 0 23 * * SUN}")
	public void executar() {
		if (!properties.isEnabled()) {
			return;
		}

		JobExecutionLogEntity log = jobExecutionLogService.start(JOB_NAME);
		try {
			ProcessarPredicoesResult result = service.processarPendentes();
			if (result.totalReservados() == 0) {
				jobExecutionLogService.markSkipped(log, "Nenhum snapshot pendente/retry para predicao.");
				return;
			}
			jobExecutionLogService.markSuccess(log, result.totalReservados(), result.totalSucesso(), result.totalFalha());
		} catch (Exception exception) {
			LOGGER.error("Erro no scheduler de predicoes ML.", exception);
			jobExecutionLogService.markFailed(log, 0, 0, 0, exception.getMessage());
		}
	}
}

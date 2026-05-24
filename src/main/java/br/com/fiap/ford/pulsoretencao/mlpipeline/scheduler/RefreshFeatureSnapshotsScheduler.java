package br.com.fiap.ford.pulsoretencao.mlpipeline.scheduler;

import br.com.fiap.ford.pulsoretencao.mlpipeline.config.RefreshFeatureSnapshotsProperties;
import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.JobExecutionLogEntity;
import br.com.fiap.ford.pulsoretencao.mlpipeline.dto.RefreshFeatureSnapshotsResult;
import br.com.fiap.ford.pulsoretencao.mlpipeline.service.JobExecutionLogService;
import br.com.fiap.ford.pulsoretencao.mlpipeline.service.RefreshFeatureSnapshotsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshFeatureSnapshotsScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshFeatureSnapshotsScheduler.class);
	private static final String JOB_NAME = "RefreshFeatureSnapshotsScheduler";

	private final RefreshFeatureSnapshotsService service;
	private final RefreshFeatureSnapshotsProperties properties;
	private final JobExecutionLogService jobExecutionLogService;

	public RefreshFeatureSnapshotsScheduler(
			RefreshFeatureSnapshotsService service,
			RefreshFeatureSnapshotsProperties properties,
			JobExecutionLogService jobExecutionLogService) {
		this.service = service;
		this.properties = properties;
		this.jobExecutionLogService = jobExecutionLogService;
	}

	@Scheduled(cron = "${app.jobs.refresh-feature-snapshots.cron:0 */30 * * * *}")
	public void executar() {
		if (!properties.isEnabled()) {
			return;
		}

		JobExecutionLogEntity log = jobExecutionLogService.start(JOB_NAME);
		try {
			RefreshFeatureSnapshotsResult result = service.executarRefresh();
			if (result.totalProcessados() == 0) {
				jobExecutionLogService.markSkipped(log, "Nenhum VIN pendente para refresh.");
				return;
			}
			jobExecutionLogService.markSuccess(log, result.totalProcessados(), result.totalProcessados(), 0);
		} catch (Exception exception) {
			LOGGER.error("Erro no scheduler de refresh de features.", exception);
			jobExecutionLogService.markFailed(log, 0, 0, 0, exception.getMessage());
		}
	}
}

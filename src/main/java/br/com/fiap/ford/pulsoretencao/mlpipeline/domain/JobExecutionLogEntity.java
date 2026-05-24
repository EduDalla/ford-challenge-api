package br.com.fiap.ford.pulsoretencao.mlpipeline.domain;

import br.com.fiap.ford.pulsoretencao.mlpipeline.enums.JobExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_execution_logs")
public class JobExecutionLogEntity {

	@Id
	private UUID id;

	@Column(name = "job_name", nullable = false, length = 120)
	private String jobName;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private JobExecutionStatus status;

	@Column(name = "started_at", nullable = false)
	private LocalDateTime startedAt;

	@Column(name = "finished_at")
	private LocalDateTime finishedAt;

	@Column(name = "duration_ms")
	private Long durationMs;

	@Column(name = "total_processed", nullable = false)
	private Integer totalProcessed = 0;

	@Column(name = "total_success", nullable = false)
	private Integer totalSuccess = 0;

	@Column(name = "total_failed", nullable = false)
	private Integer totalFailed = 0;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public static JobExecutionLogEntity start(String jobName) {
		JobExecutionLogEntity log = new JobExecutionLogEntity();
		log.id = UUID.randomUUID();
		log.jobName = jobName;
		log.status = JobExecutionStatus.RUNNING;
		log.startedAt = LocalDateTime.now();
		return log;
	}

	public void finishSuccess(int totalProcessed, int totalSuccess, int totalFailed) {
		this.status = JobExecutionStatus.SUCCESS;
		finish(totalProcessed, totalSuccess, totalFailed, null);
	}

	public void finishSkipped(String message) {
		this.status = JobExecutionStatus.SKIPPED;
		finish(0, 0, 0, message);
	}

	public void finishFailed(int totalProcessed, int totalSuccess, int totalFailed, String message) {
		this.status = JobExecutionStatus.FAILED;
		finish(totalProcessed, totalSuccess, totalFailed, message);
	}

	private void finish(int totalProcessed, int totalSuccess, int totalFailed, String message) {
		this.finishedAt = LocalDateTime.now();
		this.durationMs = Duration.between(this.startedAt, this.finishedAt).toMillis();
		this.totalProcessed = totalProcessed;
		this.totalSuccess = totalSuccess;
		this.totalFailed = totalFailed;
		this.errorMessage = message;
	}
}

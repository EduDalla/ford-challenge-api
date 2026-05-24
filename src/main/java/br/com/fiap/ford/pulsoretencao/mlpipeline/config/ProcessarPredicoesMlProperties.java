package br.com.fiap.ford.pulsoretencao.mlpipeline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jobs.processar-predicoes-ml")
public class ProcessarPredicoesMlProperties {

	private boolean enabled = true;
	private String cron = "0 0 23 * * SUN";
	private int batchSize = 100;
	private int maxAttempts = 3;
	private int retryDelayMinutes = 60;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public int getRetryDelayMinutes() {
		return retryDelayMinutes;
	}

	public void setRetryDelayMinutes(int retryDelayMinutes) {
		this.retryDelayMinutes = retryDelayMinutes;
	}
}

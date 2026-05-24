package br.com.fiap.ford.pulsoretencao.mlpipeline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jobs.refresh-feature-snapshots")
public class RefreshFeatureSnapshotsProperties {

	private boolean enabled = true;
	private String cron = "0 */30 * * * *";
	private int batchSize = 100;

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
}

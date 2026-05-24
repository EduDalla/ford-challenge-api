package br.com.fiap.ford.pulsoretencao.mlpipeline.enums;

public enum MlFeatureRefreshQueueStatus {
	PENDING("pending"),
	PROCESSING("processing"),
	DONE("done"),
	FAILED("failed");

	private final String dbValue;

	MlFeatureRefreshQueueStatus(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbValue() {
		return dbValue;
	}

	public static MlFeatureRefreshQueueStatus fromDbValue(String value) {
		for (MlFeatureRefreshQueueStatus status : values()) {
			if (status.dbValue.equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Status de fila invalido: " + value);
	}
}

package br.com.fiap.ford.pulsoretencao.mlpipeline.enums;

public enum FeatureSnapshotPredictionStatus {
	PENDING("pending"),
	PROCESSING("processing"),
	COMPLETED("completed"),
	FAILED("failed"),
	RETRY("retry");

	private final String dbValue;

	FeatureSnapshotPredictionStatus(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbValue() {
		return dbValue;
	}

	public static FeatureSnapshotPredictionStatus fromDbValue(String value) {
		for (FeatureSnapshotPredictionStatus status : values()) {
			if (status.dbValue.equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Status de predicao invalido: " + value);
	}
}

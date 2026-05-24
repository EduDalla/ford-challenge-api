package br.com.fiap.ford.pulsoretencao.mlpipeline.persistence.converter;

import br.com.fiap.ford.pulsoretencao.mlpipeline.enums.FeatureSnapshotPredictionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FeatureSnapshotPredictionStatusConverter
		implements AttributeConverter<FeatureSnapshotPredictionStatus, String> {

	@Override
	public String convertToDatabaseColumn(FeatureSnapshotPredictionStatus attribute) {
		return attribute == null ? null : attribute.getDbValue();
	}

	@Override
	public FeatureSnapshotPredictionStatus convertToEntityAttribute(String dbData) {
		return dbData == null ? null : FeatureSnapshotPredictionStatus.fromDbValue(dbData);
	}
}

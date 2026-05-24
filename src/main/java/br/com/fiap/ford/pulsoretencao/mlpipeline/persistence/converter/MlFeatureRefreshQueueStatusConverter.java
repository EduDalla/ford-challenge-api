package br.com.fiap.ford.pulsoretencao.mlpipeline.persistence.converter;

import br.com.fiap.ford.pulsoretencao.mlpipeline.enums.MlFeatureRefreshQueueStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MlFeatureRefreshQueueStatusConverter implements AttributeConverter<MlFeatureRefreshQueueStatus, String> {

	@Override
	public String convertToDatabaseColumn(MlFeatureRefreshQueueStatus attribute) {
		return attribute == null ? null : attribute.getDbValue();
	}

	@Override
	public MlFeatureRefreshQueueStatus convertToEntityAttribute(String dbData) {
		return dbData == null ? null : MlFeatureRefreshQueueStatus.fromDbValue(dbData);
	}
}

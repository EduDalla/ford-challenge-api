package br.com.fiap.ford.pulsoretencao.mlpipeline.service;

import br.com.fiap.ford.pulsoretencao.mlpipeline.config.RefreshFeatureSnapshotsProperties;
import br.com.fiap.ford.pulsoretencao.mlpipeline.dto.RefreshFeatureSnapshotsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshFeatureSnapshotsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshFeatureSnapshotsService.class);

	private final JdbcTemplate jdbcTemplate;
	private final RefreshFeatureSnapshotsProperties properties;

	public RefreshFeatureSnapshotsService(JdbcTemplate jdbcTemplate, RefreshFeatureSnapshotsProperties properties) {
		this.jdbcTemplate = jdbcTemplate;
		this.properties = properties;
	}

	@Transactional
	public RefreshFeatureSnapshotsResult executarRefresh() {
		Integer total = jdbcTemplate.queryForObject(
				"select ml.refresh_pending_features(current_date, ?)",
				Integer.class,
				properties.getBatchSize()
		);
		int processados = total == null ? 0 : total;
		LOGGER.info("Refresh de features finalizado. total_processados={}", processados);
		return new RefreshFeatureSnapshotsResult(processados);
	}
}

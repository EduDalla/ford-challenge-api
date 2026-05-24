package br.com.fiap.ford.pulsoretencao.mlpipeline.repository;

import br.com.fiap.ford.pulsoretencao.mlpipeline.domain.VinShareFeatureSnapshotEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VinShareFeatureSnapshotRepositoryImpl implements VinShareFeatureSnapshotRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@SuppressWarnings("unchecked")
	public List<VinShareFeatureSnapshotEntity> reservePendingForPrediction(int limit, int retryDelayMinutes) {
		String sql = """
				with locked as (
				    select s.id
				    from public.vin_share_feature_snapshots s
				    where s.status_predicao in ('pending', 'retry')
				      and (
				        s.status_predicao <> 'retry'
				        or s.atualizado_em <= (current_timestamp - make_interval(mins => :retryDelayMinutes))
				      )
				    order by s.criado_em
				    limit :limitRows
				    for update skip locked
				),
				updated as (
				    update public.vin_share_feature_snapshots s
				    set status_predicao = 'processing',
				        atualizado_em = current_timestamp
				    from locked l
				    where s.id = l.id
				    returning s.*
				)
				select * from updated
				""";

		return entityManager.createNativeQuery(sql, VinShareFeatureSnapshotEntity.class)
				.setParameter("retryDelayMinutes", retryDelayMinutes)
				.setParameter("limitRows", Math.max(1, limit))
				.getResultList();
	}
}

package br.com.fiap.ford.pulsoretencao.mlpipeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "predicao_resultados")
public class PredicaoResultadoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "snapshot_id", nullable = false)
	private Long snapshotId;

	@Column(name = "veiculo_id")
	private Long veiculoId;

	@Column(name = "cliente_id")
	private Long clienteId;

	@Column(name = "vin_hash", nullable = false, length = 80)
	private String vinHash;

	@Column(name = "perfil", length = 60)
	private String perfil;

	@Column(name = "risco", length = 30)
	private String risco;

	@Column(name = "score", precision = 6, scale = 4)
	private BigDecimal score;

	@Column(name = "motivo_principal")
	private String motivoPrincipal;

	@Column(name = "acao_recomendada")
	private String acaoRecomendada;

	@Column(name = "canal_recomendado", length = 40)
	private String canalRecomendado;

	@Column(name = "modelo_versao", length = 80)
	private String modeloVersao;

	@Column(name = "payload_resposta", columnDefinition = "jsonb")
	private String payloadResposta;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	public void setSnapshotId(Long snapshotId) {
		this.snapshotId = snapshotId;
	}

	public void setVeiculoId(Long veiculoId) {
		this.veiculoId = veiculoId;
	}

	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}

	public void setVinHash(String vinHash) {
		this.vinHash = vinHash;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}

	public void setRisco(String risco) {
		this.risco = risco;
	}

	public void setScore(BigDecimal score) {
		this.score = score;
	}

	public void setMotivoPrincipal(String motivoPrincipal) {
		this.motivoPrincipal = motivoPrincipal;
	}

	public void setAcaoRecomendada(String acaoRecomendada) {
		this.acaoRecomendada = acaoRecomendada;
	}

	public void setCanalRecomendado(String canalRecomendado) {
		this.canalRecomendado = canalRecomendado;
	}

	public void setModeloVersao(String modeloVersao) {
		this.modeloVersao = modeloVersao;
	}

	public void setPayloadResposta(String payloadResposta) {
		this.payloadResposta = payloadResposta;
	}
}

package br.com.fiap.ford.pulsoretencao.missao.domain;

import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import br.com.fiap.ford.pulsoretencao.profile.domain.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missoes")
public class Missao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "codigo_cartao", nullable = false, length = 40)
	private String codigoCartao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "veiculo_id", nullable = false)
	private Veiculo veiculo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "responsavel_id")
	private Profile responsavel;

	@Column(nullable = false, length = 80)
	private String perfil;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NivelRiscoMissao risco;

	@Column(nullable = false)
	private Integer score;

	@Enumerated(EnumType.STRING)
	@Column(name = "prioridade_radar", nullable = false, length = 2)
	private PrioridadeRadar prioridadeRadar;

	@Column(name = "sinais_radar", nullable = false, length = 1200)
	private String sinaisRadar;

	@Column(name = "motivo_principal", nullable = false, length = 500)
	private String motivoPrincipal;

	@Column(name = "acao_recomendada", nullable = false, length = 500)
	private String acaoRecomendada;

	@Column(name = "mensagem_sugerida", nullable = false, length = 500)
	private String mensagemSugerida;

	@Column(name = "valor_potencial", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorPotencial;

	@Column(name = "impacto_vin_share_estimado", nullable = false, precision = 5, scale = 2)
	private BigDecimal impactoVinShareEstimado;

	@Column(nullable = false, length = 80)
	private String prazo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatusMissao status = StatusMissao.EM_RISCO;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private LocalDateTime atualizadoEm;

	@OneToMany(mappedBy = "missao", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MissaoAcao> historico = new ArrayList<>();

	@OneToOne(mappedBy = "missao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private MissaoResultado resultado;

	public Missao() {
	}

	public Missao(String codigoCartao, Cliente cliente, Veiculo veiculo, String perfil, NivelRiscoMissao risco,
			Integer score, PrioridadeRadar prioridadeRadar, String sinaisRadar, String motivoPrincipal,
			String acaoRecomendada, String mensagemSugerida, BigDecimal valorPotencial,
			BigDecimal impactoVinShareEstimado, String prazo) {
		this.codigoCartao = codigoCartao;
		this.cliente = cliente;
		this.veiculo = veiculo;
		this.perfil = perfil;
		this.risco = risco;
		this.score = score;
		this.prioridadeRadar = prioridadeRadar;
		this.sinaisRadar = sinaisRadar;
		this.motivoPrincipal = motivoPrincipal;
		this.acaoRecomendada = acaoRecomendada;
		this.mensagemSugerida = mensagemSugerida;
		this.valorPotencial = valorPotencial;
		this.impactoVinShareEstimado = impactoVinShareEstimado;
		this.prazo = prazo;
		this.status = StatusMissao.EM_RISCO;
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime agora = LocalDateTime.now();
		criadoEm = agora;
		atualizadoEm = agora;
	}

	@PreUpdate
	public void preUpdate() {
		atualizadoEm = LocalDateTime.now();
	}

	public void assumir(Profile profile) {
		if (responsavel == null) {
			responsavel = profile;
		}
		status = StatusMissao.ASSUMIDO;
	}

	public void atualizarStatus(StatusMissao status) {
		this.status = status;
	}

	public void registrarAcao(MissaoAcao acao) {
		historico.add(acao);
	}

	public void atualizarResultado(MissaoResultado resultado) {
		this.resultado = resultado;
	}

	public Long getId() {
		return id;
	}

	public String getCodigoCartao() {
		return codigoCartao;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public Profile getResponsavel() {
		return responsavel;
	}

	public String getPerfil() {
		return perfil;
	}

	public NivelRiscoMissao getRisco() {
		return risco;
	}

	public Integer getScore() {
		return score;
	}

	public PrioridadeRadar getPrioridadeRadar() {
		return prioridadeRadar;
	}

	public String getSinaisRadar() {
		return sinaisRadar;
	}

	public String getMotivoPrincipal() {
		return motivoPrincipal;
	}

	public String getAcaoRecomendada() {
		return acaoRecomendada;
	}

	public String getMensagemSugerida() {
		return mensagemSugerida;
	}

	public BigDecimal getValorPotencial() {
		return valorPotencial;
	}

	public BigDecimal getImpactoVinShareEstimado() {
		return impactoVinShareEstimado;
	}

	public String getPrazo() {
		return prazo;
	}

	public StatusMissao getStatus() {
		return status;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public List<MissaoAcao> getHistorico() {
		return historico;
	}

	public MissaoResultado getResultado() {
		return resultado;
	}
}

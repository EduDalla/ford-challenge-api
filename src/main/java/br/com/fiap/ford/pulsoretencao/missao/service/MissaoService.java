package br.com.fiap.ford.pulsoretencao.missao.service;

import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import br.com.fiap.ford.pulsoretencao.cliente.domain.NivelRisco;
import br.com.fiap.ford.pulsoretencao.cliente.repository.ClienteRepository;
import br.com.fiap.ford.pulsoretencao.missao.api.AtualizarStatusMissaoRequest;
import br.com.fiap.ford.pulsoretencao.missao.api.CriarMissaoRequest;
import br.com.fiap.ford.pulsoretencao.missao.api.HistoricoAcaoResponse;
import br.com.fiap.ford.pulsoretencao.missao.api.IndicadoresRetencaoResponse;
import br.com.fiap.ford.pulsoretencao.missao.api.MissaoClienteRequest;
import br.com.fiap.ford.pulsoretencao.missao.api.MissaoClienteResponse;
import br.com.fiap.ford.pulsoretencao.missao.api.MissaoResponse;
import br.com.fiap.ford.pulsoretencao.missao.api.MissaoVeiculoRequest;
import br.com.fiap.ford.pulsoretencao.missao.api.MissaoVeiculoResponse;
import br.com.fiap.ford.pulsoretencao.missao.api.RegistrarAcaoMissaoRequest;
import br.com.fiap.ford.pulsoretencao.missao.api.RegistrarResultadoMissaoRequest;
import br.com.fiap.ford.pulsoretencao.missao.api.ResultadoMissaoResponse;
import br.com.fiap.ford.pulsoretencao.missao.domain.CanalContato;
import br.com.fiap.ford.pulsoretencao.missao.domain.Missao;
import br.com.fiap.ford.pulsoretencao.missao.domain.MissaoAcao;
import br.com.fiap.ford.pulsoretencao.missao.domain.MissaoResultado;
import br.com.fiap.ford.pulsoretencao.missao.domain.NivelRiscoMissao;
import br.com.fiap.ford.pulsoretencao.missao.domain.PrioridadeRadar;
import br.com.fiap.ford.pulsoretencao.missao.domain.StatusMissao;
import br.com.fiap.ford.pulsoretencao.missao.domain.Veiculo;
import br.com.fiap.ford.pulsoretencao.missao.repository.MissaoAcaoRepository;
import br.com.fiap.ford.pulsoretencao.missao.repository.MissaoRepository;
import br.com.fiap.ford.pulsoretencao.missao.repository.VeiculoRepository;
import br.com.fiap.ford.pulsoretencao.ml.api.MlBffPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlMissaoResponse;
import br.com.fiap.ford.pulsoretencao.ml.service.MlPredictionService;
import br.com.fiap.ford.pulsoretencao.profile.domain.PerfilProfile;
import br.com.fiap.ford.pulsoretencao.profile.domain.Profile;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import br.com.fiap.ford.pulsoretencao.shared.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MissaoService {

	private static final String SINAIS_SEPARATOR = "\n";
	private static final List<StatusMissao> STATUS_RADAR = List.of(StatusMissao.EM_RISCO, StatusMissao.REPROGRAMAR);

	private final MissaoRepository missaoRepository;
	private final MissaoAcaoRepository missaoAcaoRepository;
	private final VeiculoRepository veiculoRepository;
	private final ClienteRepository clienteRepository;
	private final MlPredictionService mlPredictionService;

	public MissaoService(MissaoRepository missaoRepository, MissaoAcaoRepository missaoAcaoRepository,
			VeiculoRepository veiculoRepository, ClienteRepository clienteRepository,
			MlPredictionService mlPredictionService) {
		this.missaoRepository = missaoRepository;
		this.missaoAcaoRepository = missaoAcaoRepository;
		this.veiculoRepository = veiculoRepository;
		this.clienteRepository = clienteRepository;
		this.mlPredictionService = mlPredictionService;
	}

	@Transactional(readOnly = true)
	public List<MissaoResponse> listar(Authentication authentication) {
		Profile profile = currentProfile(authentication);
		return missoesVisiveis(profile).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<MissaoResponse> radar(Authentication authentication) {
		Profile profile = currentProfile(authentication);
		List<Missao> missoes = isGestorOuAdmin(profile)
				? missaoRepository.findAllByStatusInOrdered(STATUS_RADAR)
				: missaoRepository.findVisibleByStatusIn(profile.getId(), STATUS_RADAR);
		return missoes.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public MissaoResponse buscarPorId(Long id, Authentication authentication) {
		Profile profile = currentProfile(authentication);
		return toResponse(buscarPermitida(id, profile));
	}

	@Transactional(readOnly = true)
	public MissaoResponse buscarPorCodigoCartao(String codigoCartao, Authentication authentication) {
		Profile profile = currentProfile(authentication);
		Missao missao = missaoRepository.findByCodigoCartaoIgnoreCase(codigoCartao.trim())
				.orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada para o cartão " + codigoCartao));
		validarAcesso(missao, profile);
		return toResponse(missao);
	}

	@Transactional
	public MissaoResponse criar(CriarMissaoRequest request, Authentication authentication) {
		Profile profile = currentProfile(authentication);
		if (!isGestorOuAdmin(profile)) {
			throw new AccessDeniedException("Apenas GESTOR ou ADMIN pode criar missões pelo Radar.");
		}

		Cliente cliente = resolverCliente(request);
		Veiculo veiculo = resolverVeiculo(request, cliente);
		MlBffPredictResponse predicao = mlPredictionService.predict(request.predicao(), null);
		MlMissaoResponse mlMissao = predicao.missao();

		String codigoCartao = resolverCodigoCartao(request.codigoCartao());
		Missao missao = new Missao(
				codigoCartao,
				cliente,
				veiculo,
				limitar(mlMissao.perfil(), 80),
				NivelRiscoMissao.from(mlMissao.risco()),
				mlMissao.score(),
				PrioridadeRadar.valueOf(mlMissao.prioridadeRadar()),
				joinSinais(mlMissao.sinaisRadar()),
				limitar(mlMissao.motivoPrincipal(), 500),
				limitar(mlMissao.acaoRecomendada(), 500),
				mensagemSugerida(mlMissao),
				valorOuPadrao(request.valorPotencial(), BigDecimal.valueOf(980)),
				valorOuPadrao(request.impactoVinShareEstimado(), BigDecimal.valueOf(1.2)),
				StringUtils.hasText(request.prazo()) ? request.prazo().trim() : "Hoje"
		);

		return toResponse(missaoRepository.save(missao));
	}

	@Transactional
	public MissaoResponse atualizarStatus(Long id, AtualizarStatusMissaoRequest request, Authentication authentication) {
		Profile profile = currentProfile(authentication);
		Missao missao = buscarPermitida(id, profile);
		aplicarStatus(missao, request.status(), profile);
		return toResponse(missaoRepository.save(missao));
	}

	@Transactional
	public MissaoResponse registrarAcao(Long id, RegistrarAcaoMissaoRequest request, Authentication authentication) {
		Profile profile = currentProfile(authentication);
		Missao missao = buscarPermitida(id, profile);
		aplicarStatus(missao, request.tipo(), profile);

		MissaoAcao acao = new MissaoAcao(
				missao,
				request.tipo(),
				request.canal(),
				StringUtils.hasText(request.observacao()) ? request.observacao().trim() : null
		);
		missao.registrarAcao(acao);
		missaoAcaoRepository.save(acao);

		if (request.resultado() != null) {
			upsertResultado(missao, request.resultado());
		}

		return toResponse(missaoRepository.save(missao));
	}

	@Transactional
	public MissaoResponse registrarResultado(Long id, RegistrarResultadoMissaoRequest request, Authentication authentication) {
		Profile profile = currentProfile(authentication);
		Missao missao = buscarPermitida(id, profile);
		upsertResultado(missao, request);
		return toResponse(missaoRepository.save(missao));
	}

	@Transactional(readOnly = true)
	public IndicadoresRetencaoResponse indicadoresConsultor(Authentication authentication) {
		Profile profile = currentProfile(authentication);
		List<Missao> missoes = missoesVisiveis(profile);
		return calcularIndicadores(missoes);
	}

	@Transactional(readOnly = true)
	public IndicadoresRetencaoResponse indicadoresRetencao(Authentication authentication) {
		Profile profile = currentProfile(authentication);
		List<Missao> missoes = isGestorOuAdmin(profile)
				? missaoRepository.findAllOrdered()
				: missoesVisiveis(profile);
		return calcularIndicadores(missoes);
	}

	private Profile currentProfile(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Profile profile)) {
			throw new AccessDeniedException("Endpoint disponível apenas para usuarios autenticados.");
		}
		return profile;
	}

	private boolean isGestorOuAdmin(Profile profile) {
		return profile.getPerfil() == PerfilProfile.ADMIN || profile.getPerfil() == PerfilProfile.GESTOR;
	}

	private List<Missao> missoesVisiveis(Profile profile) {
		return isGestorOuAdmin(profile)
				? missaoRepository.findAllOrdered()
				: missaoRepository.findVisibleToAnalista(profile.getId());
	}

	private Missao buscarPermitida(Long id, Profile profile) {
		Missao missao = missaoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Missão não encontrada com id " + id));
		validarAcesso(missao, profile);
		return missao;
	}

	private void validarAcesso(Missao missao, Profile profile) {
		if (isGestorOuAdmin(profile)) {
			return;
		}
		if (missao.getResponsavel() == null) {
			return;
		}
		UUID responsavelId = missao.getResponsavel().getId();
		if (!Objects.equals(responsavelId, profile.getId())) {
			throw new AccessDeniedException("Missão pertence a outro consultor.");
		}
	}

	private void aplicarStatus(Missao missao, StatusMissao status, Profile profile) {
		if (status == StatusMissao.ASSUMIDO) {
			missao.assumir(profile);
			return;
		}
		if (missao.getResponsavel() == null && !isGestorOuAdmin(profile)) {
			missao.assumir(profile);
		}
		missao.atualizarStatus(status);
	}

	private Cliente resolverCliente(CriarMissaoRequest request) {
		if (request.clienteId() != null) {
			return clienteRepository.findByIdAndExcluidoEmIsNull(request.clienteId())
					.orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com id " + request.clienteId()));
		}
		MissaoClienteRequest clienteRequest = request.cliente();
		if (clienteRequest == null) {
			throw new BadRequestException("Informe clienteId ou dados de cliente.");
		}
		return clienteRepository.findByDocumentoAndExcluidoEmIsNull(clienteRequest.documento())
				.or(() -> clienteRepository.findByEmailIgnoreCaseAndExcluidoEmIsNull(clienteRequest.email()))
				.orElseGet(() -> clienteRepository.save(new Cliente(
						clienteRequest.nome(),
						clienteRequest.email(),
						clienteRequest.telefone(),
						clienteRequest.documento(),
						StringUtils.hasText(clienteRequest.cidade()) ? clienteRequest.cidade() : clienteRequest.segmento(),
						NivelRisco.MEDIO,
						true
				)));
	}

	private Veiculo resolverVeiculo(CriarMissaoRequest request, Cliente cliente) {
		if (request.veiculoId() != null) {
			return veiculoRepository.findById(request.veiculoId())
					.orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com id " + request.veiculoId()));
		}
		MissaoVeiculoRequest veiculoRequest = request.veiculo();
		if (veiculoRequest == null) {
			throw new BadRequestException("Informe veiculoId ou dados de veículo.");
		}
		if (veiculoRepository.existsByVinSimulado(veiculoRequest.vinSimulado())) {
			throw new BadRequestException("Já existe veículo cadastrado com este VIN simulado.");
		}
		return veiculoRepository.save(new Veiculo(
				cliente,
				veiculoRequest.modelo(),
				veiculoRequest.ano(),
				veiculoRequest.vinSimulado(),
				veiculoRequest.kmAtual(),
				veiculoRequest.ultimaRevisao(),
				veiculoRequest.diasSemServico(),
				veiculoRequest.statusGarantia()
		));
	}

	private String resolverCodigoCartao(String codigoCartao) {
		if (StringUtils.hasText(codigoCartao)) {
			String codigo = codigoCartao.trim().toUpperCase();
			if (missaoRepository.existsByCodigoCartaoIgnoreCase(codigo)) {
				throw new BadRequestException("Já existe missão com este código de cartão.");
			}
			return codigo;
		}
		for (int tentativa = 0; tentativa < 10; tentativa++) {
			String codigo = "CARD-" + ThreadLocalRandom.current().nextInt(1000, 9999);
			if (!missaoRepository.existsByCodigoCartaoIgnoreCase(codigo)) {
				return codigo;
			}
		}
		throw new BadRequestException("Não foi possível gerar código de cartão único.");
	}

	private void upsertResultado(Missao missao, RegistrarResultadoMissaoRequest request) {
		MissaoResultado resultado = missao.getResultado();
		if (resultado == null) {
			resultado = new MissaoResultado(missao);
			missao.atualizarResultado(resultado);
		}
		resultado.atualizar(
				request.compareceu(),
				request.servicoPago(),
				request.receitaEstimada(),
				request.impactoVinShare(),
				request.proximoPasso()
		);
	}

	private IndicadoresRetencaoResponse calcularIndicadores(List<Missao> missoes) {
		long total = missoes.size();
		long altoRisco = missoes.stream().filter(m -> m.getRisco() == NivelRiscoMissao.ALTO).count();
		long contatosFeitos = missoes.stream().filter(m -> List.of(
				StatusMissao.CONTATO_FEITO,
				StatusMissao.RESPOSTA_RECEBIDA,
				StatusMissao.AGENDADO,
				StatusMissao.RECUPERADO,
				StatusMissao.PERDIDO,
				StatusMissao.REPROGRAMAR
		).contains(m.getStatus())).count();
		long agendados = missoes.stream().filter(m -> List.of(StatusMissao.AGENDADO, StatusMissao.RECUPERADO)
				.contains(m.getStatus())).count();
		long recuperados = missoes.stream().filter(m -> m.getStatus() == StatusMissao.RECUPERADO).count();
		long perdidos = missoes.stream().filter(m -> m.getStatus() == StatusMissao.PERDIDO).count();
		BigDecimal impacto = missoes.stream()
				.map(m -> m.getResultado() != null && m.getResultado().getImpactoVinShare() != null
						? m.getResultado().getImpactoVinShare()
						: BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal receita = missoes.stream()
				.filter(m -> m.getResultado() != null && Boolean.TRUE.equals(m.getResultado().getServicoPago()))
				.map(m -> m.getResultado().getReceitaEstimada() != null
						? m.getResultado().getReceitaEstimada()
						: BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return new IndicadoresRetencaoResponse(
				total,
				altoRisco,
				contatosFeitos,
				agendados,
				recuperados,
				perdidos,
				percentual(recuperados, total),
				percentual(agendados, total),
				impacto.setScale(1, RoundingMode.HALF_UP),
				receita.setScale(2, RoundingMode.HALF_UP)
		);
	}

	private int percentual(long parte, long total) {
		return total == 0 ? 0 : (int) Math.round((parte * 100.0) / total);
	}

	private MissaoResponse toResponse(Missao missao) {
		ResultadoMissaoResponse resultado = toResultado(missao.getResultado());
		List<HistoricoAcaoResponse> historico = missao.getHistorico().stream()
				.sorted(Comparator.comparing(MissaoAcao::getCriadoEm).reversed())
				.map(acao -> toHistorico(acao, null))
				.toList();
		return new MissaoResponse(
				missao.getId(),
				missao.getCodigoCartao(),
				toCliente(missao),
				toVeiculo(missao.getVeiculo()),
				missao.getPerfil(),
				missao.getRisco().getValor(),
				missao.getScore(),
				missao.getPrioridadeRadar().name(),
				toSinais(missao.getSinaisRadar()),
				missao.getMotivoPrincipal(),
				missao.getAcaoRecomendada(),
				missao.getMensagemSugerida(),
				missao.getValorPotencial(),
				missao.getImpactoVinShareEstimado(),
				missao.getPrazo(),
				missao.getResponsavel() != null ? missao.getResponsavel().getNome() : "Sem responsável",
				missao.getStatus().getValor(),
				resultado,
				historico
		);
	}

	private HistoricoAcaoResponse toHistorico(MissaoAcao acao, ResultadoMissaoResponse resultado) {
		return new HistoricoAcaoResponse(
				acao.getId(),
				acao.getTipo().getValor(),
				acao.getCanal().getValor(),
				acao.getObservacao(),
				resultado,
				acao.getCriadoEm()
		);
	}

	private MissaoClienteResponse toCliente(Missao missao) {
		Cliente cliente = missao.getCliente();
		return new MissaoClienteResponse(
				cliente.getId(),
				cliente.getNome(),
				CanalContato.WHATSAPP.getValor(),
				mascararTelefone(cliente.getTelefone()),
				StringUtils.hasText(cliente.getSegmento()) ? cliente.getSegmento() : "São Paulo",
				"Cliente sintético priorizado pelo Radar de Retenção."
		);
	}

	private MissaoVeiculoResponse toVeiculo(Veiculo veiculo) {
		return new MissaoVeiculoResponse(
				veiculo.getModelo(),
				veiculo.getAno(),
				veiculo.getVinSimulado(),
				veiculo.getKmAtual(),
				veiculo.getUltimaRevisao(),
				veiculo.getDiasSemServico(),
				veiculo.getStatusGarantia()
		);
	}

	private ResultadoMissaoResponse toResultado(MissaoResultado resultado) {
		if (resultado == null) {
			return null;
		}
		return new ResultadoMissaoResponse(
				resultado.getCompareceu(),
				resultado.getServicoPago(),
				resultado.getReceitaEstimada(),
				resultado.getImpactoVinShare(),
				resultado.getProximoPasso(),
				resultado.getAtualizadoEm()
		);
	}

	private String mascararTelefone(String telefone) {
		if (!StringUtils.hasText(telefone) || telefone.length() < 4) {
			return "(**) *****-****";
		}
		String fim = telefone.substring(Math.max(0, telefone.length() - 4));
		return "(**) *****-" + fim;
	}

	private String joinSinais(List<String> sinais) {
		if (sinais == null || sinais.isEmpty()) {
			return "Sem sinais adicionais";
		}
		return sinais.stream()
				.filter(StringUtils::hasText)
				.map(String::trim)
				.map(sinal -> limitar(sinal, 250))
				.reduce((a, b) -> a + SINAIS_SEPARATOR + b)
				.orElse("Sem sinais adicionais");
	}

	private List<String> toSinais(String sinais) {
		if (!StringUtils.hasText(sinais)) {
			return List.of();
		}
		return Arrays.stream(sinais.split(SINAIS_SEPARATOR))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.toList();
	}

	private String mensagemSugerida(MlMissaoResponse mlMissao) {
		return limitar("Olá, identificamos uma oportunidade de cuidar do seu " + mlMissao.modeloVeiculo()
				+ " na rede Ford. " + mlMissao.acaoRecomendada(), 500);
	}

	private BigDecimal valorOuPadrao(BigDecimal valor, BigDecimal padrao) {
		return valor != null ? valor : padrao;
	}

	private String limitar(String value, int max) {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		String trimmed = value.trim();
		return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
	}
}

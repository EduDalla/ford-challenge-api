package br.com.fiap.ford.pulsoretencao.interacao.service;

import br.com.fiap.ford.pulsoretencao.interacao.api.InteracaoRequest;
import br.com.fiap.ford.pulsoretencao.interacao.api.InteracaoResponse;
import br.com.fiap.ford.pulsoretencao.cliente.service.ClienteService;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import br.com.fiap.ford.pulsoretencao.shared.exception.ResourceNotFoundException;
import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import br.com.fiap.ford.pulsoretencao.interacao.domain.Interacao;
import br.com.fiap.ford.pulsoretencao.interacao.domain.TipoInteracao;
import br.com.fiap.ford.pulsoretencao.interacao.repository.InteracaoRepository;
import br.com.fiap.ford.pulsoretencao.interacao.repository.InteracaoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InteracaoService {

	private static final int TAMANHO_MAXIMO_PAGINA = 100;

	private final InteracaoRepository interacaoRepository;
	private final ClienteService clienteService;

	public InteracaoService(InteracaoRepository interacaoRepository, ClienteService clienteService) {
		this.interacaoRepository = interacaoRepository;
		this.clienteService = clienteService;
	}

	@Transactional
	public InteracaoResponse criar(Long clienteId, InteracaoRequest request) {
		validarDataInteracao(request.dataInteracao());
		Cliente cliente = clienteService.buscarEntidadePorId(clienteId);
		Interacao interacao = new Interacao(
				cliente,
				request.tipo(),
				request.descricao(),
				request.resultado(),
				request.dataInteracao()
		);
		return toResponse(interacaoRepository.save(interacao));
	}

	@Transactional(readOnly = true)
	public Page<InteracaoResponse> listarPorCliente(Long clienteId, String termo, TipoInteracao tipo,
			LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
		clienteService.buscarEntidadePorId(clienteId);
		validarIntervaloDatas(dataInicio, dataFim);

		Specification<Interacao> specification = InteracaoSpecifications.naoExcluida()
				.and(InteracaoSpecifications.clienteNaoExcluido())
				.and(InteracaoSpecifications.clienteId(clienteId))
				.and(InteracaoSpecifications.texto(termo))
				.and(InteracaoSpecifications.tipo(tipo))
				.and(InteracaoSpecifications.dataInteracaoMaiorOuIgual(dataInicio))
				.and(InteracaoSpecifications.dataInteracaoMenorOuIgual(dataFim));

		return interacaoRepository.findAll(specification, limitarTamanhoPagina(pageable))
				.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public InteracaoResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Transactional
	public void excluir(Long id) {
		Interacao interacao = buscarEntidadePorId(id);
		interacao.excluir(LocalDateTime.now());
		interacaoRepository.save(interacao);
	}

	private Interacao buscarEntidadePorId(Long id) {
		return interacaoRepository.findAtivaById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Interação não encontrada com id " + id));
	}

	private void validarDataInteracao(LocalDateTime dataInteracao) {
		if (dataInteracao != null && dataInteracao.isAfter(LocalDateTime.now())) {
			throw new BadRequestException("A data da interação não pode estar no futuro.");
		}
	}

	private void validarIntervaloDatas(LocalDateTime dataInicio, LocalDateTime dataFim) {
		if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
			throw new BadRequestException("A data inicial não pode ser posterior à data final.");
		}
	}

	private InteracaoResponse toResponse(Interacao interacao) {
		return new InteracaoResponse(
				interacao.getId(),
				interacao.getCliente().getId(),
				interacao.getTipo(),
				interacao.getDescricao(),
				interacao.getResultado(),
				interacao.getDataInteracao(),
				interacao.getExcluidoEm()
		);
	}

	private Pageable limitarTamanhoPagina(Pageable pageable) {
		if (pageable.getPageSize() <= TAMANHO_MAXIMO_PAGINA) {
			return pageable;
		}
		return PageRequest.of(pageable.getPageNumber(), TAMANHO_MAXIMO_PAGINA, pageable.getSort());
	}
}

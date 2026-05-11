package br.com.fiap.ford.pulsoretencao.interacao;

import br.com.fiap.ford.pulsoretencao.interacao.api.InteracaoRequest;
import br.com.fiap.ford.pulsoretencao.interacao.api.InteracaoResponse;
import br.com.fiap.ford.pulsoretencao.cliente.ClienteService;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import br.com.fiap.ford.pulsoretencao.shared.exception.DatabaseException;
import br.com.fiap.ford.pulsoretencao.shared.exception.ResourceNotFoundException;
import br.com.fiap.ford.pulsoretencao.cliente.Cliente;
import br.com.fiap.ford.pulsoretencao.interacao.Interacao;
import br.com.fiap.ford.pulsoretencao.interacao.InteracaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteracaoService {

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
	public List<InteracaoResponse> listarPorCliente(Long clienteId) {
		clienteService.buscarEntidadePorId(clienteId);
		return interacaoRepository.findByClienteIdOrderByDataInteracaoDesc(clienteId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public InteracaoResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Transactional
	public void excluir(Long id) {
		if (!interacaoRepository.existsById(id)) {
			throw new ResourceNotFoundException("Interação não encontrada com id " + id);
		}

		try {
			interacaoRepository.deleteById(id);
		} catch (DataIntegrityViolationException exception) {
			throw new DatabaseException("Não foi possível excluir a interação por restrição do banco de dados.");
		}
	}

	private Interacao buscarEntidadePorId(Long id) {
		return interacaoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Interação não encontrada com id " + id));
	}

	private void validarDataInteracao(LocalDateTime dataInteracao) {
		if (dataInteracao != null && dataInteracao.isAfter(LocalDateTime.now())) {
			throw new BadRequestException("A data da interação não pode estar no futuro.");
		}
	}

	private InteracaoResponse toResponse(Interacao interacao) {
		return new InteracaoResponse(
				interacao.getId(),
				interacao.getCliente().getId(),
				interacao.getCliente().getNome(),
				interacao.getTipo(),
				interacao.getDescricao(),
				interacao.getResultado(),
				interacao.getDataInteracao()
		);
	}
}

package br.com.fiap.ford.pulsoretencao.cliente;

import br.com.fiap.ford.pulsoretencao.cliente.api.ClienteRequest;
import br.com.fiap.ford.pulsoretencao.cliente.api.ClienteResponse;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import br.com.fiap.ford.pulsoretencao.shared.exception.DatabaseException;
import br.com.fiap.ford.pulsoretencao.shared.exception.ResourceNotFoundException;
import br.com.fiap.ford.pulsoretencao.cliente.Cliente;
import br.com.fiap.ford.pulsoretencao.cliente.ClienteRepository;
import br.com.fiap.ford.pulsoretencao.interacao.InteracaoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

	private final ClienteRepository clienteRepository;
	private final InteracaoRepository interacaoRepository;

	public ClienteService(ClienteRepository clienteRepository, InteracaoRepository interacaoRepository) {
		this.clienteRepository = clienteRepository;
		this.interacaoRepository = interacaoRepository;
	}

	@Transactional
	public ClienteResponse criar(ClienteRequest request) {
		validarEmailEDocumentoParaCriacao(request);
		Cliente cliente = new Cliente(
				request.nome(),
				request.email(),
				request.telefone(),
				request.documento(),
				request.segmento(),
				request.nivelRisco(),
				request.ativo()
		);
		return toResponse(clienteRepository.save(cliente));
	}

	@Transactional(readOnly = true)
	public List<ClienteResponse> listar() {
		return clienteRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Transactional
	public ClienteResponse atualizar(Long id, ClienteRequest request) {
		Cliente cliente = buscarEntidadePorId(id);
		validarEmailEDocumentoParaAtualizacao(id, request);

		cliente.setNome(request.nome());
		cliente.setEmail(request.email());
		cliente.setTelefone(request.telefone());
		cliente.setDocumento(request.documento());
		cliente.setSegmento(request.segmento());
		cliente.setNivelRisco(request.nivelRisco());
		cliente.setAtivo(request.ativo() != null ? request.ativo() : cliente.getAtivo());

		return toResponse(clienteRepository.save(cliente));
	}

	@Transactional
	public void excluir(Long id) {
		if (!clienteRepository.existsById(id)) {
			throw new ResourceNotFoundException("Cliente não encontrado com id " + id);
		}

		try {
			clienteRepository.deleteById(id);
		} catch (DataIntegrityViolationException exception) {
			throw new DatabaseException("Não foi possível excluir o cliente por restrição do banco de dados.");
		}
	}

	@Transactional(readOnly = true)
	public Cliente buscarEntidadePorId(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com id " + id));
	}

	private void validarEmailEDocumentoParaCriacao(ClienteRequest request) {
		if (clienteRepository.existsByEmail(request.email())) {
			throw new BadRequestException("Já existe um cliente cadastrado com este email.");
		}
		if (clienteRepository.existsByDocumento(request.documento())) {
			throw new BadRequestException("Já existe um cliente cadastrado com este documento.");
		}
	}

	private void validarEmailEDocumentoParaAtualizacao(Long id, ClienteRequest request) {
		if (clienteRepository.existsByEmailAndIdNot(request.email(), id)) {
			throw new BadRequestException("Já existe outro cliente cadastrado com este email.");
		}
		if (clienteRepository.existsByDocumentoAndIdNot(request.documento(), id)) {
			throw new BadRequestException("Já existe outro cliente cadastrado com este documento.");
		}
	}

	private ClienteResponse toResponse(Cliente cliente) {
		long totalInteracoes = interacaoRepository.countByClienteId(cliente.getId());
		return new ClienteResponse(
				cliente.getId(),
				cliente.getNome(),
				cliente.getEmail(),
				cliente.getTelefone(),
				cliente.getDocumento(),
				cliente.getSegmento(),
				cliente.getNivelRisco(),
				cliente.getAtivo(),
				cliente.getCriadoEm(),
				cliente.getAtualizadoEm(),
				totalInteracoes
		);
	}
}

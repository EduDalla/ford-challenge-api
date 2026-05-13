package br.com.fiap.ford.pulsoretencao.cliente.service;

import br.com.fiap.ford.pulsoretencao.cliente.api.ClienteRequest;
import br.com.fiap.ford.pulsoretencao.cliente.api.ClienteResponse;
import br.com.fiap.ford.pulsoretencao.cliente.domain.NivelRisco;
import br.com.fiap.ford.pulsoretencao.cliente.repository.ClienteSpecifications;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import br.com.fiap.ford.pulsoretencao.shared.exception.ResourceNotFoundException;
import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import br.com.fiap.ford.pulsoretencao.cliente.repository.ClienteRepository;
import br.com.fiap.ford.pulsoretencao.interacao.repository.InteracaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ClienteService {

	private static final int TAMANHO_MAXIMO_PAGINA = 100;

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
	public Page<ClienteResponse> listar(String termo, NivelRisco nivelRisco, Boolean ativo, Pageable pageable) {
		Specification<Cliente> specification = ClienteSpecifications.naoExcluido()
				.and(ClienteSpecifications.texto(termo))
				.and(ClienteSpecifications.nivelRisco(nivelRisco))
				.and(ClienteSpecifications.ativo(ativo));

		return clienteRepository.findAll(specification, limitarTamanhoPagina(pageable))
				.map(this::toResponse);
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
		Cliente cliente = buscarEntidadePorId(id);
		LocalDateTime dataExclusao = LocalDateTime.now();
		cliente.excluir(dataExclusao);
		interacaoRepository.softDeleteByClienteId(cliente.getId(), dataExclusao);
		clienteRepository.save(cliente);
	}

	@Transactional(readOnly = true)
	public Cliente buscarEntidadePorId(Long id) {
		return clienteRepository.findByIdAndExcluidoEmIsNull(id)
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
				cliente.getExcluidoEm()
		);
	}

	private Pageable limitarTamanhoPagina(Pageable pageable) {
		if (pageable.getPageSize() <= TAMANHO_MAXIMO_PAGINA) {
			return pageable;
		}
		return PageRequest.of(pageable.getPageNumber(), TAMANHO_MAXIMO_PAGINA, pageable.getSort());
	}
}

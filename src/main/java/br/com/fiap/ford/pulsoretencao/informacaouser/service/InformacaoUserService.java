package br.com.fiap.ford.pulsoretencao.informacaouser.service;

import br.com.fiap.ford.pulsoretencao.informacao.domain.Informacao;
import br.com.fiap.ford.pulsoretencao.informacao.service.InformacaoService;
import br.com.fiap.ford.pulsoretencao.informacaouser.api.InformacaoUserRequest;
import br.com.fiap.ford.pulsoretencao.informacaouser.api.InformacaoUserResponse;
import br.com.fiap.ford.pulsoretencao.informacaouser.domain.InformacaoUser;
import br.com.fiap.ford.pulsoretencao.informacaouser.repository.InformacaoUserRepository;
import br.com.fiap.ford.pulsoretencao.informacaouser.repository.InformacaoUserSpecifications;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import br.com.fiap.ford.pulsoretencao.shared.exception.ResourceNotFoundException;
import br.com.fiap.ford.pulsoretencao.usuario.domain.Usuario;
import br.com.fiap.ford.pulsoretencao.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class InformacaoUserService {

	private static final int TAMANHO_MAXIMO_PAGINA = 100;

	private final InformacaoUserRepository informacaoUserRepository;
	private final UsuarioRepository usuarioRepository;
	private final InformacaoService informacaoService;

	public InformacaoUserService(InformacaoUserRepository informacaoUserRepository, UsuarioRepository usuarioRepository,
			InformacaoService informacaoService) {
		this.informacaoUserRepository = informacaoUserRepository;
		this.usuarioRepository = usuarioRepository;
		this.informacaoService = informacaoService;
	}

	@Transactional
	public InformacaoUserResponse criar(InformacaoUserRequest request) {
		validarDuplicidadeCriacao(request.userId(), request.informacaoId());

		Usuario usuario = buscarUsuarioPorId(request.userId());
		Informacao informacao = informacaoService.buscarEntidadeAtivaPorId(request.informacaoId());
		InformacaoUser informacaoUser = new InformacaoUser(usuario, informacao, request.dataAlerta());

		return toResponse(informacaoUserRepository.save(informacaoUser));
	}

	@Transactional(readOnly = true)
	public Page<InformacaoUserResponse> listar(Long userId, Long informacaoId, LocalDate dataAlerta, Pageable pageable) {
		Specification<InformacaoUser> specification = InformacaoUserSpecifications.userId(userId)
				.and(InformacaoUserSpecifications.informacaoId(informacaoId))
				.and(InformacaoUserSpecifications.dataAlerta(dataAlerta));

		return informacaoUserRepository.findAll(specification, limitarTamanhoPagina(pageable))
				.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public InformacaoUserResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Transactional
	public InformacaoUserResponse atualizar(Long id, InformacaoUserRequest request) {
		InformacaoUser informacaoUser = buscarEntidadePorId(id);
		validarDuplicidadeAtualizacao(id, request.userId(), request.informacaoId());

		Usuario usuario = buscarUsuarioPorId(request.userId());
		Informacao informacao = informacaoService.buscarEntidadeAtivaPorId(request.informacaoId());

		informacaoUser.setUsuario(usuario);
		informacaoUser.setInformacao(informacao);
		informacaoUser.setDataAlerta(request.dataAlerta());

		return toResponse(informacaoUserRepository.save(informacaoUser));
	}

	@Transactional
	public void excluir(Long id) {
		InformacaoUser informacaoUser = buscarEntidadePorId(id);
		informacaoUserRepository.delete(informacaoUser);
	}

	private InformacaoUser buscarEntidadePorId(Long id) {
		return informacaoUserRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Informação do usuário não encontrada com id " + id));
	}

	private Usuario buscarUsuarioPorId(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id " + id));
	}

	private void validarDuplicidadeCriacao(Long userId, Long informacaoId) {
		if (informacaoUserRepository.existsByUsuarioIdAndInformacaoId(userId, informacaoId)) {
			throw new BadRequestException("Este usuário já possui vínculo com a informação informada.");
		}
	}

	private void validarDuplicidadeAtualizacao(Long id, Long userId, Long informacaoId) {
		if (informacaoUserRepository.existsByUsuarioIdAndInformacaoIdAndIdNot(userId, informacaoId, id)) {
			throw new BadRequestException("Este usuário já possui vínculo com a informação informada.");
		}
	}

	private InformacaoUserResponse toResponse(InformacaoUser informacaoUser) {
		return new InformacaoUserResponse(
				informacaoUser.getId(),
				informacaoUser.getUsuario().getId(),
				informacaoUser.getInformacao().getId(),
				informacaoUser.getDataAlerta(),
				informacaoUser.getCriadoEm()
		);
	}

	private Pageable limitarTamanhoPagina(Pageable pageable) {
		if (pageable.getPageSize() <= TAMANHO_MAXIMO_PAGINA) {
			return pageable;
		}
		return PageRequest.of(pageable.getPageNumber(), TAMANHO_MAXIMO_PAGINA, pageable.getSort());
	}
}

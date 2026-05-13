package br.com.fiap.ford.pulsoretencao.informacao.service;

import br.com.fiap.ford.pulsoretencao.informacao.api.InformacaoRequest;
import br.com.fiap.ford.pulsoretencao.informacao.api.InformacaoResponse;
import br.com.fiap.ford.pulsoretencao.informacao.domain.Informacao;
import br.com.fiap.ford.pulsoretencao.informacao.repository.InformacaoRepository;
import br.com.fiap.ford.pulsoretencao.informacao.repository.InformacaoSpecifications;
import br.com.fiap.ford.pulsoretencao.shared.exception.BadRequestException;
import br.com.fiap.ford.pulsoretencao.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InformacaoService {

	private static final int TAMANHO_MAXIMO_PAGINA = 100;

	private final InformacaoRepository informacaoRepository;

	public InformacaoService(InformacaoRepository informacaoRepository) {
		this.informacaoRepository = informacaoRepository;
	}

	@Transactional
	public InformacaoResponse criar(InformacaoRequest request) {
		validarNomeParaCriacao(request.nome());
		Informacao informacao = new Informacao(request.nome(), request.descricao(), request.ativo());
		return toResponse(informacaoRepository.save(informacao));
	}

	@Transactional(readOnly = true)
	public Page<InformacaoResponse> listar(String termo, Boolean ativo, Pageable pageable) {
		Specification<Informacao> specification = InformacaoSpecifications.texto(termo)
				.and(InformacaoSpecifications.ativo(ativo));

		return informacaoRepository.findAll(specification, limitarTamanhoPagina(pageable))
				.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public InformacaoResponse buscarPorId(Long id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Transactional
	public InformacaoResponse atualizar(Long id, InformacaoRequest request) {
		Informacao informacao = buscarEntidadePorId(id);
		validarNomeParaAtualizacao(id, request.nome());

		informacao.setNome(request.nome());
		informacao.setDescricao(request.descricao());
		informacao.setAtivo(request.ativo() != null ? request.ativo() : informacao.getAtivo());

		return toResponse(informacaoRepository.save(informacao));
	}

	@Transactional
	public void desativar(Long id) {
		Informacao informacao = buscarEntidadePorId(id);
		informacao.setAtivo(false);
		informacaoRepository.save(informacao);
	}

	@Transactional(readOnly = true)
	public Informacao buscarEntidadePorId(Long id) {
		return informacaoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Informação não encontrada com id " + id));
	}

	@Transactional(readOnly = true)
	public Informacao buscarEntidadeAtivaPorId(Long id) {
		return informacaoRepository.findByIdAndAtivoTrue(id)
				.orElseThrow(() -> new BadRequestException("A informação informada não está ativa."));
	}

	private void validarNomeParaCriacao(String nome) {
		if (informacaoRepository.existsByNome(nome)) {
			throw new BadRequestException("Já existe uma informação cadastrada com este nome.");
		}
	}

	private void validarNomeParaAtualizacao(Long id, String nome) {
		if (informacaoRepository.existsByNomeAndIdNot(nome, id)) {
			throw new BadRequestException("Já existe outra informação cadastrada com este nome.");
		}
	}

	private InformacaoResponse toResponse(Informacao informacao) {
		return new InformacaoResponse(
				informacao.getId(),
				informacao.getNome(),
				informacao.getDescricao(),
				informacao.getAtivo(),
				informacao.getCriadoEm(),
				informacao.getAtualizadoEm()
		);
	}

	private Pageable limitarTamanhoPagina(Pageable pageable) {
		if (pageable.getPageSize() <= TAMANHO_MAXIMO_PAGINA) {
			return pageable;
		}
		return PageRequest.of(pageable.getPageNumber(), TAMANHO_MAXIMO_PAGINA, pageable.getSort());
	}
}

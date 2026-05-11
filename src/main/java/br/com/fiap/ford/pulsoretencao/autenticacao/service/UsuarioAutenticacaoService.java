package br.com.fiap.ford.pulsoretencao.autenticacao.service;

import br.com.fiap.ford.pulsoretencao.autenticacao.repository.UsuarioAutenticacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioAutenticacaoService {

	private final UsuarioAutenticacaoRepository repository;

	public UsuarioAutenticacaoService(UsuarioAutenticacaoRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void registrarLoginComSucesso(String login) {
		repository.findByUsuarioLogin(login)
				.ifPresent(autenticacao -> {
					autenticacao.registrarLoginComSucesso();
					repository.save(autenticacao);
				});
	}

	@Transactional
	public void registrarFalhaLogin(String login) {
		repository.findByUsuarioLogin(login)
				.ifPresent(autenticacao -> {
					autenticacao.registrarFalhaLogin();
					repository.save(autenticacao);
				});
	}
}

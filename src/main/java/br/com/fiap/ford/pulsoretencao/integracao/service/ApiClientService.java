package br.com.fiap.ford.pulsoretencao.integracao.service;

import br.com.fiap.ford.pulsoretencao.autenticacao.api.DadosAutenticacaoServico;
import br.com.fiap.ford.pulsoretencao.integracao.domain.ApiClient;
import br.com.fiap.ford.pulsoretencao.integracao.repository.ApiClientRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiClientService {

	private final ApiClientRepository repository;
	private final PasswordEncoder passwordEncoder;

	public ApiClientService(ApiClientRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public ApiClient autenticar(DadosAutenticacaoServico dados) {
		ApiClient apiClient = repository.findByClientIdAndAtivoTrue(dados.clientId())
				.orElseThrow(() -> new BadCredentialsException("Cliente técnico inválido."));

		if (!passwordEncoder.matches(dados.clientSecret(), apiClient.getClientSecretHash())) {
			throw new BadCredentialsException("Cliente técnico inválido.");
		}

		apiClient.registrarUso();
		return repository.save(apiClient);
	}
}

package br.com.fiap.ford.pulsoretencao.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioAutenticacaoRepository extends JpaRepository<UsuarioAutenticacao, Long> {

	Optional<UsuarioAutenticacao> findByUsuarioLogin(String login);
}

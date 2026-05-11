package br.com.fiap.ford.pulsoretencao.usuario.repository;

import br.com.fiap.ford.pulsoretencao.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "autenticacao")
    Optional<Usuario> findByLoginAndAtivoTrue(String login);

    default UserDetails findByLogin(String username) {
        return findByLoginAndAtivoTrue(username).orElse(null);
    }
}

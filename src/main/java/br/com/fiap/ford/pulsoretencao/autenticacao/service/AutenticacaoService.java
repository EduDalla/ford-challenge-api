package br.com.fiap.ford.pulsoretencao.autenticacao.service;

import br.com.fiap.ford.pulsoretencao.usuario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioRepository repository;

    public AutenticacaoService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByLoginAndAtivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado ou inativo."));
    }
}

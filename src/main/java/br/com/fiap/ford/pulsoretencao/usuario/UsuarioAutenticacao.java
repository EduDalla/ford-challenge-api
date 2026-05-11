package br.com.fiap.ford.pulsoretencao.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_autenticacao")
public class UsuarioAutenticacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true)
	private Usuario usuario;

	@Column(name = "tentativas_login", nullable = false)
	private Integer tentativasLogin = 0;

	@Column(name = "bloqueado_ate")
	private LocalDateTime bloqueadoAte;

	@Column(name = "senha_alterada_em", nullable = false)
	private LocalDateTime senhaAlteradaEm;

	@Column(name = "ultimo_login_em")
	private LocalDateTime ultimoLoginEm;

	public UsuarioAutenticacao() {
	}

	public UsuarioAutenticacao(Usuario usuario) {
		this.usuario = usuario;
		this.senhaAlteradaEm = LocalDateTime.now();
	}

	public boolean estaBloqueado() {
		return bloqueadoAte != null && bloqueadoAte.isAfter(LocalDateTime.now());
	}

	public void registrarLoginComSucesso() {
		tentativasLogin = 0;
		bloqueadoAte = null;
		ultimoLoginEm = LocalDateTime.now();
	}

	public void registrarFalhaLogin() {
		tentativasLogin = tentativasLogin == null ? 1 : tentativasLogin + 1;
		if (tentativasLogin >= 5) {
			bloqueadoAte = LocalDateTime.now().plusMinutes(15);
		}
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Integer getTentativasLogin() {
		return tentativasLogin;
	}

	public LocalDateTime getBloqueadoAte() {
		return bloqueadoAte;
	}

	public LocalDateTime getSenhaAlteradaEm() {
		return senhaAlteradaEm;
	}

	public LocalDateTime getUltimoLoginEm() {
		return ultimoLoginEm;
	}
}

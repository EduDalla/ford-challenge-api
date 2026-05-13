package br.com.fiap.ford.pulsoretencao.informacaouser.repository;

import br.com.fiap.ford.pulsoretencao.informacaouser.domain.InformacaoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InformacaoUserRepository extends JpaRepository<InformacaoUser, Long>, JpaSpecificationExecutor<InformacaoUser> {

	boolean existsByUsuarioIdAndInformacaoId(Long userId, Long informacaoId);

	boolean existsByUsuarioIdAndInformacaoIdAndIdNot(Long userId, Long informacaoId, Long id);
}

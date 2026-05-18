package br.com.fiap.ford.pulsoretencao.informacaouser.repository;

import br.com.fiap.ford.pulsoretencao.informacaouser.domain.InformacaoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InformacaoUserRepository extends JpaRepository<InformacaoUser, Long>, JpaSpecificationExecutor<InformacaoUser> {

	boolean existsByProfileIdAndInformacaoId(UUID userId, Long informacaoId);

	boolean existsByProfileIdAndInformacaoIdAndIdNot(UUID userId, Long informacaoId, Long id);
}

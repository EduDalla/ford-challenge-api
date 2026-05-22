package br.com.fiap.ford.pulsoretencao.missao.repository;

import br.com.fiap.ford.pulsoretencao.missao.domain.MissaoAcao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissaoAcaoRepository extends JpaRepository<MissaoAcao, Long> {
}

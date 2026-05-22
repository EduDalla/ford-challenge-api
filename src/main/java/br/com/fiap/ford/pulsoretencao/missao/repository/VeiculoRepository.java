package br.com.fiap.ford.pulsoretencao.missao.repository;

import br.com.fiap.ford.pulsoretencao.missao.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

	boolean existsByVinSimulado(String vinSimulado);
}

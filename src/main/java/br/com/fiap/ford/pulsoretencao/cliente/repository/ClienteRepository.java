package br.com.fiap.ford.pulsoretencao.cliente.repository;

import br.com.fiap.ford.pulsoretencao.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

	Optional<Cliente> findByIdAndExcluidoEmIsNull(Long id);

	boolean existsByEmail(String email);

	boolean existsByDocumento(String documento);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByDocumentoAndIdNot(String documento, Long id);
}

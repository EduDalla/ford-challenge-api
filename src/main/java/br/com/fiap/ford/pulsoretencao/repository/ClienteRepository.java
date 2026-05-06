package br.com.fiap.ford.pulsoretencao.repository;

import br.com.fiap.ford.pulsoretencao.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	boolean existsByEmail(String email);

	boolean existsByDocumento(String documento);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByDocumentoAndIdNot(String documento, Long id);
}

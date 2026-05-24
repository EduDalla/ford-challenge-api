package br.com.fiap.ford.pulsoretencao.cliente.api;

import br.com.fiap.ford.pulsoretencao.cliente.api.ClienteRequest;
import br.com.fiap.ford.pulsoretencao.cliente.api.ClienteResponse;
import br.com.fiap.ford.pulsoretencao.cliente.domain.NivelRisco;
import br.com.fiap.ford.pulsoretencao.cliente.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Operações de cadastro e acompanhamento de clientes")
@PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
public class ClienteController {

	private final ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@PostMapping
	@Operation(summary = "Cadastrar cliente")
	public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
		ClienteResponse response = clienteService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	@Operation(summary = "Listar clientes")
	public ResponseEntity<Page<ClienteResponse>> listar(
			@RequestParam(required = false, name = "q") String termo,
			@RequestParam(required = false) NivelRisco nivelRisco,
			@RequestParam(required = false) Boolean ativo,
			@PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(clienteService.listar(termo, nivelRisco, ativo, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar cliente por ID")
	public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(clienteService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar cliente")
	public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody ClienteRequest request) {
		return ResponseEntity.ok(clienteService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir cliente logicamente")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		clienteService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}

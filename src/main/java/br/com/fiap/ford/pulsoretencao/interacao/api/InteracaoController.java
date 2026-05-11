package br.com.fiap.ford.pulsoretencao.interacao.api;

import br.com.fiap.ford.pulsoretencao.interacao.api.InteracaoRequest;
import br.com.fiap.ford.pulsoretencao.interacao.api.InteracaoResponse;
import br.com.fiap.ford.pulsoretencao.interacao.InteracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Interações", description = "Registro de contatos e ações de retenção")
public class InteracaoController {

	private final InteracaoService interacaoService;

	public InteracaoController(InteracaoService interacaoService) {
		this.interacaoService = interacaoService;
	}

	@PostMapping("/clientes/{clienteId}/interacoes")
	@Operation(summary = "Registrar interação de retenção para um cliente")
	public ResponseEntity<InteracaoResponse> criar(@PathVariable Long clienteId,
			@Valid @RequestBody InteracaoRequest request) {
		InteracaoResponse response = interacaoService.criar(clienteId, request);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/v1/interacoes/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/clientes/{clienteId}/interacoes")
	@Operation(summary = "Listar interações de um cliente")
	public ResponseEntity<List<InteracaoResponse>> listarPorCliente(@PathVariable Long clienteId) {
		return ResponseEntity.ok(interacaoService.listarPorCliente(clienteId));
	}

	@GetMapping("/interacoes/{id}")
	@Operation(summary = "Buscar interação por ID")
	public ResponseEntity<InteracaoResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(interacaoService.buscarPorId(id));
	}

	@DeleteMapping("/interacoes/{id}")
	@Operation(summary = "Excluir interação")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		interacaoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}

package br.com.fiap.ford.pulsoretencao.informacao.api;

import br.com.fiap.ford.pulsoretencao.informacao.service.InformacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/informacoes")
@Tag(name = "Informações", description = "Catálogo de informações relevantes para consultores")
public class InformacaoController {

	private final InformacaoService informacaoService;

	public InformacaoController(InformacaoService informacaoService) {
		this.informacaoService = informacaoService;
	}

	@PostMapping
	@Operation(summary = "Cadastrar informação")
	public ResponseEntity<InformacaoResponse> criar(@Valid @RequestBody InformacaoRequest request) {
		InformacaoResponse response = informacaoService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	@Operation(summary = "Listar informações")
	public ResponseEntity<Page<InformacaoResponse>> listar(
			@RequestParam(required = false, name = "q") String termo,
			@RequestParam(required = false) Boolean ativo,
			@PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(informacaoService.listar(termo, ativo, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar informação por ID")
	public ResponseEntity<InformacaoResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(informacaoService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar informação")
	public ResponseEntity<InformacaoResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody InformacaoRequest request) {
		return ResponseEntity.ok(informacaoService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Desativar informação")
	public ResponseEntity<Void> desativar(@PathVariable Long id) {
		informacaoService.desativar(id);
		return ResponseEntity.noContent().build();
	}
}

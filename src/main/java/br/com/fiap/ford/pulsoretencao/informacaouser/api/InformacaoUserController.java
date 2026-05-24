package br.com.fiap.ford.pulsoretencao.informacaouser.api;

import br.com.fiap.ford.pulsoretencao.informacaouser.service.InformacaoUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/informacoes-user")
@Tag(name = "Informacoes do Usuario", description = "Vinculos de informacoes relevantes por consultor")
@PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
public class InformacaoUserController {

	private final InformacaoUserService informacaoUserService;

	public InformacaoUserController(InformacaoUserService informacaoUserService) {
		this.informacaoUserService = informacaoUserService;
	}

	@PostMapping
	@Operation(summary = "Cadastrar vinculo de informacao para usuario")
	public ResponseEntity<InformacaoUserResponse> criar(@Valid @RequestBody InformacaoUserRequest request) {
		InformacaoUserResponse response = informacaoUserService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	@Operation(summary = "Listar vinculos de informacoes de usuarios")
	public ResponseEntity<Page<InformacaoUserResponse>> listar(
			@RequestParam(required = false) UUID userId,
			@RequestParam(required = false) Long informacaoId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAlerta,
			@PageableDefault(size = 20, sort = "dataAlerta", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(informacaoUserService.listar(userId, informacaoId, dataAlerta, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar vinculo por ID")
	public ResponseEntity<InformacaoUserResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(informacaoUserService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar vinculo de informacao do usuario")
	public ResponseEntity<InformacaoUserResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody InformacaoUserRequest request) {
		return ResponseEntity.ok(informacaoUserService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir vinculo de informacao do usuario")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		informacaoUserService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}

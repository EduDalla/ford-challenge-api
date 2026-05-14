package br.com.fiap.ford.pulsoretencao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health", description = "Endpoints leves de liveness para deploy")
public class HealthController {

	@GetMapping("/")
	@Operation(summary = "Liveness da API")
	public Map<String, String> root() {
		return Map.of(
				"status", "ok",
				"service", "ford-challenge-api"
		);
	}

	@GetMapping({"/health", "/healthCheck"})
	@Operation(summary = "Health check da API")
	public Map<String, String> health() {
		return Map.of("status", "ok");
	}
}

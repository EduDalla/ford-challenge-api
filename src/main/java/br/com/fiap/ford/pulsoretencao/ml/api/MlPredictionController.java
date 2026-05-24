package br.com.fiap.ford.pulsoretencao.ml.api;

import br.com.fiap.ford.pulsoretencao.ml.api.MlBffPredictResponse;
import br.com.fiap.ford.pulsoretencao.ml.api.MlPredictRequest;
import br.com.fiap.ford.pulsoretencao.ml.service.MlPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ml")
@Tag(name = "ML BFF", description = "Integra o Java com a FastAPI ML publicada no Render")
public class MlPredictionController {

	private final MlPredictionService mlPredictionService;

	public MlPredictionController(MlPredictionService mlPredictionService) {
		this.mlPredictionService = mlPredictionService;
	}

	@PostMapping("/predict")
	@Operation(summary = "Gerar predicao integrada Java -> FastAPI ML")
	public ResponseEntity<MlBffPredictResponse> predict(
			@Parameter(description = "Token de demo opcional para teste manual no Swagger Java quando DEMO_MODE=true. O BFF nao encaminha este valor para a FastAPI.")
			@RequestHeader(value = "X-ML-Demo-Token", required = false) String demoToken,
			@Valid @RequestBody MlPredictRequest request) {
		return ResponseEntity.ok(mlPredictionService.predict(request));
	}
}

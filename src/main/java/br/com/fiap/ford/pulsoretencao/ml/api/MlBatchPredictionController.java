package br.com.fiap.ford.pulsoretencao.ml.api;

import br.com.fiap.ford.pulsoretencao.ml.service.MlBatchPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/ml/predicoes")
@Tag(name = "ML Batch", description = "Orquestra a fila de snapshots e a IA em lote")
public class MlBatchPredictionController {

	private final MlBatchPredictionService mlBatchPredictionService;

	public MlBatchPredictionController(MlBatchPredictionService mlBatchPredictionService) {
		this.mlBatchPredictionService = mlBatchPredictionService;
	}

	@PostMapping("/processar-lote")
	@Operation(summary = "Reservar snapshots prontos, chamar a IA em lote e salvar predicoes")
	public ResponseEntity<MlBatchProcessResponse> processarLote(
			@Parameter(description = "Quantidade maxima de snapshots reservados nesta chamada")
			@RequestParam(defaultValue = "100") @Min(1) @Max(500) int limit,
			@Parameter(description = "JWT demo opcional para teste manual no Swagger Java. Em producao, o BFF usa FORD_ML_SERVICE_TOKEN como X-ML-Service-Token.")
			@RequestHeader(value = "X-ML-Demo-Token", required = false) String tokenOverride) {
		return ResponseEntity.ok(mlBatchPredictionService.processarLote(limit, tokenOverride));
	}
}

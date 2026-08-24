package cars.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cars.model.QueryRequest;
import cars.service.OrcestratorService;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class QueryController {
    private final OrcestratorService orcestratorService;

    public QueryController(OrcestratorService orcestratorService) {
        this.orcestratorService = orcestratorService;
    }

    @PostMapping("/query")
    public ResponseEntity<?> handleQuery(@RequestBody QueryRequest request) {
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Query-ul nu poate fi gol."));
        }
        try {
            String response = orcestratorService.processQuery(request.getQuery());
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

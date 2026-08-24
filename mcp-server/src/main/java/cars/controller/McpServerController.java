package cars.controller;

import cars.model.JsonRequest;
import cars.model.JsonResponse;
import cars.service.ToolChooserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
public class McpServerController {

    private final ToolChooserService toolChooserService;

    private McpServerController(ToolChooserService toolChooserService) {
        this.toolChooserService = toolChooserService;
    }

    @PostMapping("/rpc")
    public ResponseEntity<JsonResponse> handleRpc(@RequestBody JsonRequest request) {
        if(!"2.0".equals(request.getJsonrpc())) {
            return ResponseEntity.badRequest().body(JsonResponse.error(request.getId(), -32600, "Invalid JSON-RPC version"));
        }

        try {
            Object result = toolChooserService.chooseTool(request.getMethod(), request.getParams());
            return ResponseEntity.ok(JsonResponse.success(result, request.getId()));
        } catch (IllegalAccessException e) {
            return ResponseEntity.badRequest().body(JsonResponse.error(request.getId(), -32601, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(JsonResponse.error(request.getId(), -32000, "Eroare: " + e.getMessage()));
        }
    }

    @GetMapping("/tools")
    public ResponseEntity<String> getTools() {
        return ResponseEntity.ok(toolChooserService.getToolsSchema());
    }
}

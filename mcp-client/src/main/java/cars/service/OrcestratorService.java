package cars.service;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class OrcestratorService {
    private final GeminiService geminiService;
    private final McpServerProxy mcpServerProxy;

    public OrcestratorService(GeminiService geminiService, McpServerProxy mcpServerProxy) {
        this.geminiService = geminiService;
        this.mcpServerProxy = mcpServerProxy;
    }

    public String processQuery(String userQuery) throws Exception {
        System.out.println(">>> Pasul 1: citesc schema tool-urilor");
        String toolSchema = mcpServerProxy.getToolSchema();
        System.out.println(">>> Schema OK: " + toolSchema.substring(0, 50));

        System.out.println(">>> Pasul 2: intreb Gemini");
        Map<String, Object> toolCall = geminiService.selectTool(userQuery, toolSchema);
        String toolName = (String) toolCall.get("toolName");
        Map<String, Object> params = (Map<String, Object>) toolCall.get("params");
        System.out.println(">>> Tool ales: " + toolName + " | Params: " + params);

        System.out.println(">>> Pasul 3: execut tool prin JSON-RPC");
        String toolResult = mcpServerProxy.callTool(toolName, params);
        System.out.println(">>> Rezultat: " + toolResult.substring(0, Math.min(200, toolResult.length())));

        System.out.println(">>> Pasul 4: Gemini formuleaza raspuns");
        return geminiService.formatResponse(userQuery,toolName,toolResult);
    }
}

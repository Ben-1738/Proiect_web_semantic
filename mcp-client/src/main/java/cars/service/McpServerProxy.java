package cars.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.Map;
import java.util.UUID;

@Service
public class McpServerProxy {

    @Value("${mcp.server.url}")
    private String mcpServerUrl;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callTool(String toolName, Map<String, Object> param) throws Exception {
        Map<String, Object> rcpRequest = Map.of(
                "jsonrpc", "2.0",
                "method", toolName,
                "params", param,
                "id", UUID.randomUUID().toString()
        );

        String body = objectMapper.writeValueAsString(rcpRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mcpServerUrl + "/rpc"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(">>> Raspuns raw de la MCP Server: " + response.body());
        JsonNode jsonNode = objectMapper.readTree(response.body());

        if(jsonNode.has("error") && !jsonNode.get("error").isNull()) {
            String errorMsg = jsonNode.get("error").path("message").asText("Unknown error");
            System.out.println(">>> Eroare RPC: " + errorMsg);
            throw new RuntimeException("RPC Error: " + errorMsg);
        }
        return jsonNode.get("result").toString();
    }

    public String getToolSchema() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mcpServerUrl + "/tools"))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body()
        ;
    }
}

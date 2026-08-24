package cars.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String extractTextFromGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        if (root.has("error")) {
            String errorMsg = root.path("error").path("message").asText("Unknown error");
            throw new RuntimeException("Gemini API error: " + errorMsg);
        }

        JsonNode candidatesNode = root.path("candidates");
        if (candidatesNode.isMissingNode() || !candidatesNode.isArray() || candidatesNode.isEmpty()) {
            throw new RuntimeException("Gemini API nu a returnat 'candidates'. Raspuns complet: " + responseBody);
        }

        JsonNode firstCandidate = candidatesNode.get(0);
        if (firstCandidate == null) {
            throw new RuntimeException("Gemini API: candidates[0] este null. Raspuns complet: " + responseBody);
        }

        JsonNode finishReason = firstCandidate.path("finishReason");
        if (!finishReason.isMissingNode() && "SAFETY".equals(finishReason.asText())) {
            throw new RuntimeException("Gemini API: raspunsul a fost blocat de safety filters.");
        }

        JsonNode contentNode = firstCandidate.path("content");
        if (contentNode.isMissingNode()) {
            throw new RuntimeException("Gemini API: candidates[0].content lipseste. Candidate: " + firstCandidate);
        }

        JsonNode partsNode = contentNode.path("parts");
        if (partsNode.isMissingNode() || !partsNode.isArray() || partsNode.isEmpty()) {
            throw new RuntimeException("Gemini API: parts lipseste sau e gol. Content: " + contentNode);
        }

        JsonNode firstPart = partsNode.get(0);
        if (firstPart == null) {
            throw new RuntimeException("Gemini API: parts[0] este null. Parts: " + partsNode);
        }

        return firstPart.path("text").asText("");
    }

    public Map<String, Object> selectTool(String userQuery, String toolSchema) throws Exception {
        String prompt = """
                 Esti un asistent pentru gestiunea bazei de date cu masini.
                            Ai disponibile urmatoarele tool-uri:
                            %s
                
                            Cererea utilizatorului: "%s"
                
                            Raspunde DOAR cu un JSON valid, fara text suplimentar, fara backtick-uri:
                            {
                              "toolName": "numele_tool_ului",
                              "params": { "param1": "valoare1" }
                            }
                """.formatted(toolSchema, userQuery);

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{ Map.of("text", prompt) })
                },
                "generationConfig", Map.of("temperature", 0.1)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(geminiApiUrl + "?key=" + geminiApiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(">>> Gemini selectTool status: " + response.statusCode());
        System.out.println(">>> Gemini selectTool raw: " + response.body());

        String text = extractTextFromGeminiResponse(response.body());
        text = text.replaceAll("```json", "").replaceAll("```", "").trim();
        System.out.println(">>> Gemini selectTool text extras: " + text);

        JsonNode toolCall = objectMapper.readTree(text);
        String toolName = toolCall.get("toolName").asText();
        Map<String, Object> params = objectMapper.convertValue(toolCall.get("params"), Map.class);

        return Map.of("toolName", toolName, "params", params);
    }

    public String formatResponse(String userQuery, String toolName, String toolResult) throws Exception {
        String prompt = """
            Utilizatorul a cerut: "%s"
            Am executat operatia "%s" si am obtinut:
            %s

            Formuleaza un raspuns natural si clar in romana,
            prezentand datele intr-un mod usor de inteles.
            """.formatted(userQuery, toolName, toolResult);

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{ Map.of("text", prompt) })
                }
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(geminiApiUrl + "?key=" + geminiApiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(">>> Gemini formatResponse status: " + response.statusCode());
        System.out.println(">>> Gemini formatResponse raw: " + response.body());

        return extractTextFromGeminiResponse(response.body());
    }
}

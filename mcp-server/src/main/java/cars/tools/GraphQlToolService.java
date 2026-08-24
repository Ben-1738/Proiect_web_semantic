package cars.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class GraphQlToolService {
    @Value("${graphql.server.url}")
    private String graphQlServerUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String executePost(String body) throws Exception {
        URL url = new URL(graphQlServerUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        StringBuilder response = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                response.append(buffer, 0, read);
            }
        }
        return response.toString();
    }

    public String getModelsByFuelType(String fuelType) throws Exception {
        String body = """
                {
                  "query": "{ allModels(filter: { tipCombustibil: \\"%s\\" }) { id nume brandId tipCombustibil capacitateMotor putereCP anFabricatie } }"
                }
                """
                .formatted(fuelType);

        return executePost(body);
    }

    public String addModel(Map<String, Object> param) throws Exception {
        String nume = (String) param.get("nume");
        String brandId = String.valueOf(param.get("brandId"));
        String tipCombustibil = (String) param.get("tipCombustibil");
        int capacitateMotor = Integer.parseInt(String.valueOf(param.get("capacitateMotor")));
        int putereCP = Integer.parseInt(String.valueOf(param.get("putereCP")));
        int anFabricatie = Integer.parseInt(String.valueOf(param.get("anFabricatie")));

        String body = """
                {
                "query": "mutation { createModel(nume: \\"%s\\", brandId: %s, tipCombustibil: \\"%s\\", capacitateMotor: %d, putereCP: %d, anFabricatie: %d) { id nume brandId tipCombustibil capacitateMotor putereCP anFabricatie } }"
                }
                """
                .formatted(nume, brandId, tipCombustibil, capacitateMotor, putereCP, anFabricatie);

        return executePost(body);
    }
}

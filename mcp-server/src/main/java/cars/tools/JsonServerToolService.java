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
public class JsonServerToolService {

    @Value("${json.server.url}")
    private String jsonServerUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String executeRequest(String urlStr, String method, String body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (body != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
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

    public String getBrandsByCountry(String country) throws Exception {
        String url = jsonServerUrl + "/brands?tara=" + country;
        System.out.println(">>> JsonServer URL: " + url);
        return executeRequest(url, "GET", null);
    }

    public String addBrand(Map<String, Object> brandData) throws Exception {
        String requestBody = objectMapper.writeValueAsString(brandData);
        return executeRequest(jsonServerUrl + "/brands", "POST", requestBody);
    }
}

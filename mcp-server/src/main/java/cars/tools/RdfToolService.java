package cars.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class RdfToolService {

    @Value("${rdf4j.server.url}")
    private String rdf4jUrl;

    private String executeGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/sparql-results+json");
        
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

    private String executePost(String urlStr, String body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/sparql-update");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        int statusCode = conn.getResponseCode();
        if (statusCode == 204) {
            return "Car added successfully";
        }
        
        StringBuilder response = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                response.append(buffer, 0, read);
            }
        }
        return "Failed to add car: " + response.toString();
    }

    public String getCarsByBrand(String brand) throws Exception {
        String sparql = """
                PREFIX schema: <http://schema.org/>
                SELECT ?car ?name ?horsepower ?makeYear WHERE {
                    ?car a schema:Car ;
                         schema:name ?name ;
                         schema:brand ?brand ;
                         schema:horsepower ?horsepower ;
                         schema:makeYear ?makeYear .
                    ?brand schema:name "%s" .
                }
                """.formatted(brand);

        String encodedQuery = URLEncoder.encode(sparql, StandardCharsets.UTF_8);
        return executeGet(rdf4jUrl + "?query=" + encodedQuery);
    }

    public String getAllCars() throws Exception {
        String sparql = """
                PREFIX schema: <http://schema.org/>
                SELECT ?car ?name ?brandName ?horsepower ?makeYear ?description WHERE {
                    ?car a schema:Car ;
                         schema:name ?name ;
                         schema:brand ?brand .
                    ?brand schema:name ?brandName .
                    OPTIONAL { ?car schema:horsepower ?horsepower }
                    OPTIONAL { ?car schema:makeYear ?makeYear }
                    OPTIONAL { ?car schema:description ?description }
                }
                """;

        String encodedQuery = URLEncoder.encode(sparql, StandardCharsets.UTF_8);
        return executeGet(rdf4jUrl + "?query=" + encodedQuery);
    }

    public String addCars(Map<String, Object> param) throws Exception {
        String id = (String) param.get("id");
        String model = (String) param.get("model");
        String brand = (String) param.get("brandNume");
        String motorizare = String.valueOf(param.get("motorizare"));
        String an = String.valueOf(param.get("an"));

        String brandUri = brand.toLowerCase().replace(" ", "-");

        String sparql = """
                PREFIX schema: <http://schema.org/>
                INSERT DATA {
                    <https://cars.com/brand/%s> a schema:Brand ;
                        schema:name "%s" .
                    <https://cars.com/model/%s> a schema:Car ;
                        schema:name "%s" ;
                        schema:brand <https://cars.com/brand/%s> ;
                        schema:description "%s" ;
                        schema:makeYear "%s" .
                }
                """.formatted(brandUri, brand, id, model, brandUri, motorizare, an);

        return executePost(rdf4jUrl + "/statements", sparql);
    }
}

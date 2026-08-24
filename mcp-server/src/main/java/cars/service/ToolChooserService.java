package cars.service;
import cars.tools.GraphQlToolService;
import cars.tools.JsonServerToolService;
import cars.tools.RdfToolService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ToolChooserService {

    private final JsonServerToolService jsonServerToolService;
    private final GraphQlToolService graphQlToolService;
    private final RdfToolService rdfToolService;

    public ToolChooserService (JsonServerToolService jsonService, GraphQlToolService graphQlService, RdfToolService rdfService) {
        this.jsonServerToolService = jsonService;
        this.graphQlToolService = graphQlService;
        this.rdfToolService = rdfService;
    }

    public Object chooseTool(String toolName, Map<String, Object> params) throws Exception {
        System.out.println(">>> MCP Server toolName: '" + toolName + "'");
        System.out.println(">>> Params: " + params);
        return switch (toolName) {
            case "getBrandsByCountry" -> jsonServerToolService.getBrandsByCountry((String) params.get("tara"));
            case "addBrand" -> jsonServerToolService.addBrand(params);
            case "getModelsByFuelType" -> graphQlToolService.getModelsByFuelType((String) params.get("tipCombustibil"));
            case "addModel" -> graphQlToolService.addModel(params);
            case "getMasiniByBrand" -> {
                String brand = (String) params.get("brandNume");
                if (brand == null || brand.trim().isEmpty() || brand.equals("UNKNOWN_OR_ALL_BRANDS_NOT_SUPPORTED")) {
                    yield rdfToolService.getAllCars();
                } else {
                    yield rdfToolService.getCarsByBrand(brand);
                }
            }
            case "getAllMasini" -> rdfToolService.getAllCars();
            case "addMasina" -> rdfToolService.addCars(params);
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }

    public String getToolsSchema(){
        return """
                [
                          { "name": "getBrandsByCountry",
                            "description": "Returneaza brandurile dintr-o tara din JSON-Server",
                            "parameters": { "tara": "string - ex: Germania, Japonia" } },
                          { "name": "addBrand",
                            "description": "Adauga un brand nou in JSON-Server",
                            "parameters": { "id": "string", "nume": "string", "tara": "string",
                                            "anInfiintare": "number", "sediu": "string" } },
                          { "name": "getModelsByFuelType",
                            "description": "Returneaza modelele dupa combustibil din GraphQL-Server",
                            "parameters": { "tipCombustibil": "string - benzina/diesel/electric/hibrid" } },
                          { "name": "addModel",
                            "description": "Adauga un model nou in GraphQL-Server",
                            "parameters": { "nume": "string", "brandId": "string", "tipCombustibil": "string",
                                            "capacitateMotor": "number", "putereCP": "number", "anFabricatie": "number" } },
                          { "name": "getMasiniByBrand",
                            "description": "Returneaza masinile unui brand din RDF4J via SPARQL",
                            "parameters": { "brandNume": "string - ex: BMW, Toyota" } },
                          { "name": "getAllMasini",
                            "description": "Returneaza absolut toate masinile (indiferent de brand) din RDF4J via SPARQL",
                            "parameters": {} },
                          { "name": "addMasina",
                            "description": "Adauga o masina in graful RDF4J",
                            "parameters": { "id": "string", "model": "string", "brandNume": "string",
                                            "motorizare": "string", "an": "string" } }
                        ]
                """;
    }
}

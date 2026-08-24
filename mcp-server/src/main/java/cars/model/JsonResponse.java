package cars.model;

public class JsonResponse {
    private String jsonrpc = "2.0";
    private Object result;
    private JsonError error;
    private String id;

    public static JsonResponse success(Object result, String id) {
        JsonResponse response = new JsonResponse();
        response.result = result;
        response.id = id;
        return response;
    }

    public static JsonResponse error(String id, int code, String message) {
        JsonResponse response = new JsonResponse();
        response.error = new JsonError(code, message);
        response.id = id;
        return response;
    }

    public static class JsonError {
        private int code;
        private String message;

        public JsonError(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public Object getResult() {
        return result;
    }

    public JsonError getError() {
        return error;
    }

    public String getId() {
        return id;
    }
}

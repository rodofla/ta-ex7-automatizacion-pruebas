package cl.iplacex.automatizacion;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


public class App {

    private static HttpServer server;
    private static final TareaService tareaService = new TareaService();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String version = System.getenv().getOrDefault("APP_VERSION", "1.0.0");
        iniciarServidor(port, version);
        System.out.println("Servidor iniciado en puerto " + port + " (version " + version + ")");
    }

    public static HttpServer iniciarServidor(int port, String version) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        
        server.createContext("/health", exchange -> {
            String body = "{\"status\":\"UP\",\"version\":\"" + version + "\"}";
            enviarRespuesta(exchange, 200, body);
        });

        
        server.createContext("/api/tareas", exchange -> {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                String jsonArray = tareaService.listarTareas().stream()
                        .map(t -> "\"" + t + "\"")
                        .collect(Collectors.joining(",", "[", "]"));
                enviarRespuesta(exchange, 200, "{\"tareas\":" + jsonArray + "}");
            } else if ("POST".equalsIgnoreCase(method)) {
                String payload = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    String creada = tareaService.agregarTarea(payload);
                    enviarRespuesta(exchange, 201, "{\"creada\":\"" + creada + "\"}");
                } catch (IllegalArgumentException e) {
                    enviarRespuesta(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else {
                enviarRespuesta(exchange, 405, "{\"error\":\"Metodo no permitido\"}");
            }
        });

        server.setExecutor(null);
        server.start();
        return server;
    }

    public static void detenerServidor() {
        if (server != null) {
            server.stop(0);
        }
    }

    public static TareaService getTareaService() {
        return tareaService;
    }

    private static void enviarRespuesta(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

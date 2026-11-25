package cl.iplacex.automatizacion;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;


@Tag("integration")
class TareaApiIT {

    private static final int TEST_PORT = 8085;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private static HttpClient httpClient;

    @BeforeAll
    static void iniciarAmbiente() throws IOException {
        App.iniciarServidor(TEST_PORT, "1.0.0-it");
        httpClient = HttpClient.newHttpClient();
    }

    @BeforeEach
    void limpiarEstado() {
        
        App.getTareaService().limpiar();
    }

    @AfterAll
    static void detenerAmbiente() {
        App.detenerServidor();
    }

    @Test
    @DisplayName("GET /health debe responder HTTP 200 con estado UP")
    void debeResponderHealthCheck() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    @Test
    @DisplayName("POST y GET /api/tareas deben persistir y consultar tareas via HTTP")
    void debeCrearYListarTareasViaApi() throws Exception {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/tareas"))
                .POST(HttpRequest.BodyPublishers.ofString("Ejecutar pruebas de integracion"))
                .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());
        assertTrue(postResponse.body().contains("Ejecutar pruebas de integracion"));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/tareas"))
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Ejecutar pruebas de integracion"));
    }

    @Test
    @DisplayName("POST /api/tareas con cuerpo vacio debe retornar HTTP 400")
    void debeRetornarBadRequestSiPayloadEsVacio() throws Exception {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/tareas"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
    }
}

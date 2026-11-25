package cl.iplacex.automatizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;


public class TareaAcceptanceSteps {

    private String baseUrl;
    private boolean servidorLocalIniciado = false;
    private HttpClient client;
    private HttpResponse<String> lastResponse;

    @Before
    public void prepararAmbiente() throws IOException {
        client = HttpClient.newHttpClient();
        String stagingUrl = System.getenv("STAGING_URL");
        if (stagingUrl != null && !stagingUrl.isBlank()) {
            baseUrl = stagingUrl;
        } else {
            int ephemeralPort = 8089;
            App.getTareaService().limpiar();
            App.iniciarServidor(ephemeralPort, "1.0.0-bdd");
            baseUrl = "http://localhost:" + ephemeralPort;
            servidorLocalIniciado = true;
        }
    }

    @After
    public void limpiarAmbiente() {
        if (servidorLocalIniciado) {
            App.detenerServidor();
            servidorLocalIniciado = false;
        }
    }

    @Given("que el servicio se encuentra desplegado en el ambiente de pruebas")
    public void verificarAmbienteDisponible() {
        assertNotNull(baseUrl);
    }

    @When("consulto el endpoint de monitoreo {string}")
    public void consultarEndpoint(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @When("registro una nueva tarea llamada {string}")
    public void registrarTarea(String titulo) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tareas"))
                .POST(HttpRequest.BodyPublishers.ofString(titulo))
                .build();
        lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Then("la respuesta debe tener codigo HTTP {int}")
    public void validarCodigoHttp(int expectedStatus) {
        assertEquals(expectedStatus, lastResponse.statusCode());
    }

    @Then("la creacion debe responder con codigo HTTP {int}")
    public void validarCreacionHttp(int expectedStatus) {
        assertEquals(expectedStatus, lastResponse.statusCode());
    }

    @And("el cuerpo de la respuesta debe indicar estado {string}")
    public void validarEstadoUp(String estado) {
        assertTrue(lastResponse.body().contains("\"status\":\"" + estado + "\""));
    }

    @And("al consultar {string} debe aparecer {string}")
    public void validarTareaListada(String path, String titulo) throws Exception {
        consultarEndpoint(path);
        assertEquals(200, lastResponse.statusCode());
        assertTrue(lastResponse.body().contains(titulo));
    }
}

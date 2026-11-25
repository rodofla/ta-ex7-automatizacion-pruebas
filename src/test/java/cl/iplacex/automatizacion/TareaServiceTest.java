package cl.iplacex.automatizacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TareaServiceTest {

    private TareaService service;

    @BeforeEach
    void setUp() {
        service = new TareaService();
    }

    @Test
    @DisplayName("Debe agregar una tarea valida y listarla correctamente")
    void debeAgregarTareaValida() {
        String resultado = service.agregarTarea("Configurar pipeline CI");
        assertEquals("Configurar pipeline CI", resultado);
        assertEquals(1, service.listarTareas().size());
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el titulo esta vacio o nulo")
    void debeRechazarTituloVacio() {
        assertThrows(IllegalArgumentException.class, () -> service.agregarTarea("   "));
        assertThrows(IllegalArgumentException.class, () -> service.agregarTarea(null));
    }

    @Test
    @DisplayName("Debe calcular correctamente el porcentaje de avance")
    void debeCalcularPorcentajeAvance() {
        service.agregarTarea("Tarea 1");
        service.agregarTarea("Tarea 2");
        service.completarTarea("Tarea 1");

        assertEquals(50.0, service.calcularPorcentajeAvance(), 0.01);
    }
}

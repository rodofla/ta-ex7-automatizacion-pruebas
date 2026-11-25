package cl.iplacex.automatizacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TareaService {

    private final List<String> tareas = new ArrayList<>();
    private int completadas = 0;

   
    public String agregarTarea(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El titulo de la tarea no puede estar vacio");
        }
        String limpia = titulo.trim();
        tareas.add(limpia);
        return limpia;
    }

   
    public boolean completarTarea(String titulo) {
        if (titulo != null && tareas.contains(titulo.trim())) {
            completadas++;
            return true;
        }
        return false;
    }

   
    public List<String> listarTareas() {
        return Collections.unmodifiableList(tareas);
    }

    
    public double calcularPorcentajeAvance() {
        if (tareas.isEmpty()) {
            return 0.0;
        }
        return (completadas * 100.0) / tareas.size();
    }

    
    public void limpiar() {
        tareas.clear();
        completadas = 0;
    }
}

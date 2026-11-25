@acceptance
Feature: Criterios de Aceptacion del Servicio Gestor de Tareas
  Como responsable de calidad y negocio
  Quiero validar los flujos criticos en el ambiente desplegado
  Para autorizar el paso a produccion mediante el Acceptance Gate

  Scenario: Verificacion de salud del servicio desplegado
    Given que el servicio se encuentra desplegado en el ambiente de pruebas
    When consulto el endpoint de monitoreo "/health"
    Then la respuesta debe tener codigo HTTP 200
    And el cuerpo de la respuesta debe indicar estado "UP"

  Scenario: Flujo completo de registro y consulta de tarea
    Given que el servicio se encuentra desplegado en el ambiente de pruebas
    When registro una nueva tarea llamada "Validar despliegue Blue-Green"
    Then la creacion debe responder con codigo HTTP 201
    And al consultar "/api/tareas" debe aparecer "Validar despliegue Blue-Green"

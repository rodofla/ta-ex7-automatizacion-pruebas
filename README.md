# Gestor de Tareas CI/CD — Examen Final (TA_EX_7)

Proyecto Java 17 y Apache Maven desarrollado para la asignatura **Automatización de Pruebas (IPLACEX)**, articulando control de configuración con **GitFlow**, pruebas automatizadas multinivel (**JUnit 5, Selenium y Cucumber BDD**), integración continua con **GitHub Actions**, despliegue **Blue-Green** con **Docker + NGINX** y **Rollback automático**.

## Estrategia de Pruebas Implementada

1. **Pruebas Unitarias (`*Test.java` - Maven Surefire):** Validan en aislamiento la lógica de negocio de `TareaService` con retroalimentación rápida (*Fail-Fast*).
2. **Pruebas de Integración (`*IT.java` - Maven Failsafe):** Levantan un servidor HTTP embebido en puerto dedicado (`8085`) con limpieza de estado (`@BeforeEach`) para garantizar **idempotencia** en los contratos REST (`/health` y `/api/tareas`).
3. **Pruebas de Aceptación BDD (`@acceptance` - Cucumber Gherkin):** Actúan como **Automated Acceptance Test Gate** contra el contenedor de *Staging* antes de autorizar el paso a producción.

## Cómo Ejecutar Localmente

```bash
# 1. Ejecutar pruebas unitarias (Surefire)
mvn clean test

# 2. Ejecutar pruebas de integracion HTTP (Failsafe)
mvn verify -Dsurefire.skip=true

# 3. Ejecutar pruebas de aceptacion BDD (Cucumber)
mvn test -Dtest=RunAcceptanceTest

# 4. Empaquetar JAR y construir imagen Docker
mvn clean package -DskipTests
docker build -t gestor-tareas:latest .

# 5. Probar despliegue Blue-Green y Rollback en contenedores
./infra/deploy-blue-green.sh
./infra/rollback.sh
```

## Pipelines CI/CD Versionados

- **Integración Continua (`.github/workflows/ci-pipeline.yml`):** Ejecuta secuencialmente `Checkout & Build` → `Pruebas Unitarias` → `Pruebas de Integración` y publica los reportes XML como artefactos.
- **Entrega y Despliegue Continuo (`.github/workflows/cd-deployment-pipeline.yml`):** Construye la imagen Docker, despliega en `Staging`, valida el `Acceptance Gate (BDD)`, ejecuta el switch **Blue-Green (`1.0.0-BLUE` -> `2.0.0-GREEN`)** y dispara automáticamente el **Rollback** ante fallos post-despliegue, registrando trazabilidad en `deploy-metadata.json`.

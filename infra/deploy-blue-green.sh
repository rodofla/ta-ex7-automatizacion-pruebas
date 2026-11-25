#!/usr/bin/env bash
set -e

echo "=================================================================="
echo " INICIANDO ESTRATEGIA DE DESPLIEGUE BLUE-GREEN"
echo "=================================================================="

# 1. Crear red aislada para comunicacion entre balanceador y entornos
docker network create blue-green-net 2>/dev/null || true

# 2. Levantar entorno BLUE (version estable v1.0.0 actualmente en produccion)
docker rm -f app-blue app-green lb-router 2>/dev/null || true
docker run -d --name app-blue --network blue-green-net -p 8081:8080 \
  -e PORT=8080 -e APP_VERSION="1.0.0-BLUE" gestor-tareas:latest

# 3. Configurar balanceador NGINX apuntando inicialmente a BLUE
cat << 'EOF' > infra/nginx.conf
events {}
http {
  server {
    listen 80;
    location / {
      proxy_pass http://app-blue:8080;
    }
  }
}
EOF

docker run -d --name lb-router --network blue-green-net -p 8080:80 \
  -v "$(pwd)/infra/nginx.conf:/etc/nginx/nginx.conf:ro" nginx:alpine

sleep 2
echo "[ESTADO INICIAL] Trafico activo en entorno BLUE:"
curl -s http://localhost:8080/health && echo ""

# 4. Desplegar nueva version candidata en entorno GREEN (v2.0.0)
echo "[DESPLIEGUE GREEN] Levantando contenedor app-green (v2.0.0-GREEN)..."
docker run -d --name app-green --network blue-green-net -p 8082:8080 \
  -e PORT=8080 -e APP_VERSION="2.0.0-GREEN" gestor-tareas:latest
sleep 2

# 5. Ejecutar Smoke Test interno sobre GREEN antes de conmutar trafico
echo "[SMOKE TEST GREEN] Validando salud de app-green en puerto 8082..."
curl -sf http://localhost:8082/health | grep '"status":"UP"'

# 6. Switch de trafico (Zero-Downtime) de BLUE -> GREEN
echo "[SWITCH TRAFICO] Redirigiendo balanceador NGINX hacia entorno GREEN..."
cat << 'EOF' > infra/nginx.conf
events {}
http {
  server {
    listen 80;
    location / {
      proxy_pass http://app-green:8080;
    }
  }
}
EOF
docker exec lb-router nginx -s reload
sleep 1

echo "[ESTADO POST-SWITCH] Trafico activo en entorno GREEN:"
curl -s http://localhost:8080/health && echo ""
echo "=================================================================="
echo " DESPLIEGUE BLUE-GREEN COMPLETADO EXITOSAMENTE"
echo "=================================================================="

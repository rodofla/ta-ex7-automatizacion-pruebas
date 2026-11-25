#!/usr/bin/env bash
set -e

echo "=================================================================="
echo " [ALERTA CRITICA] FALLO DETECTADO POST-DESPLIEGUE EN GREEN"
echo " INICIANDO ROLLBACK AUTOMATICO HACIA ENTORNO ESTABLE BLUE"
echo "=================================================================="

# 1. Revertir configuracion del balanceador NGINX hacia app-blue (v1.0.0-BLUE)
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

docker exec lb-router nginx -s reload
sleep 1

# 2. Retirar contenedor defectuoso GREEN
echo "[LIMPIEZA] Deteniendo y eliminando contenedor defectuoso app-green..."
docker stop app-green && docker rm app-green

# 3. Verificar restauracion del servicio estable
echo "[VERIFICACION] Comprobando endpoint principal tras rollback:"
RESPONSE=$(curl -sf http://localhost:8080/health)
echo "$RESPONSE"
echo "$RESPONSE" | grep '"version":"1.0.0-BLUE"'

echo "=================================================================="
echo " [OK] ROLLBACK COMPLETADO: SERVICIO RESTAURADO A 1.0.0-BLUE"
echo "=================================================================="

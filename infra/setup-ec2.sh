#!/bin/bash
# =============================================================================
# Configura a EC2 (Ubuntu) para rodar a API + Watchtower.
# Uso, dentro da EC2:   bash setup-ec2.sh SEU_USUARIO_DOCKERHUB
# =============================================================================
set -euo pipefail

RA="01251069"
DOCKERHUB_USERNAME="${1:?Informe o usuario do Docker Hub: bash setup-ec2.sh seuusuario}"
IMAGE="${DOCKERHUB_USERNAME}/api-produtos-${RA}:latest"
API="api-produtos-${RA}"
WATCHTOWER="watchtower-${RA}"

# 1) Docker (mesmos comandos do material da aula)
if ! command -v docker >/dev/null 2>&1; then
  sudo apt update
  sudo apt install -y docker.io
  sudo systemctl enable --now docker
  sudo usermod -a -G docker "$(whoami)"
fi

# Remove containers de versões anteriores do script (nomes sem RA), se existirem
sudo docker rm -f api-produtos watchtower 2>/dev/null || true

# 2) API (container monitorado pelo Watchtower por causa da label)
sudo docker pull "$IMAGE"
sudo docker rm -f "$API" 2>/dev/null || true
sudo docker run -d \
  --name "$API" \
  --restart unless-stopped \
  -p 8080:8080 \
  --label com.centurylinklabs.watchtower.enable=true \
  "$IMAGE"

# 3) Watchtower: verifica o Docker Hub a cada 30s e atualiza a API sozinho.
#    Fork mantido; o containrrr/watchtower original foi arquivado e falha no Docker 29+.
sudo docker rm -f "$WATCHTOWER" 2>/dev/null || true
sudo docker run -d \
  --name "$WATCHTOWER" \
  --restart unless-stopped \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -e TZ=America/Sao_Paulo \
  nickfedor/watchtower \
  --label-enable --cleanup --interval 30 --debug

sudo docker ps
echo
echo "Pronto! Acesse: http://<IP-PUBLICO>:8080/api/info"

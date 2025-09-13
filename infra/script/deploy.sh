#!/bin/bash

# 안전하게 스크립트 실행 중 에러 발생 시 종료
set -e


IMAGE_NAME="ji0513ji/bobeat:latest"
CONTAINER_NAME="bobeat"
DOCKERFILE_PATH="infra/docker/Dockerfile"

docker pull $IMAGE_NAME

echo "📦 Step 1: 기존 컨테이너 중지 및 삭제"

# 컨테이너 중지 (존재할 경우)
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
  docker stop $CONTAINER_NAME
fi

# 컨테이너 삭제 (존재할 경우)
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
  docker rm $CONTAINER_NAME
fi


echo "🚀 Step 3: 새 컨테이너 실행"
docker compose -f infra/docker/docker-compose.yml up -d

echo "✅ 배포 완료: 컨테이너 '$CONTAINER_NAME' 실행 중"

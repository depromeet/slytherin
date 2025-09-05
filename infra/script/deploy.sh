#!/bin/bash

APP_NAME="bobeat-server.jar"
JAR_PATH="./build/libs/bobeat-server.jar"
LOG_PATH="./application.log"
echo "🔍 실행 중인 애플리케이션 확인"
PID=$(ps -ef | grep $APP_NAME | grep java | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
  echo "🚫 실행 중인 애플리케이션이 없습니다."
else
  echo "🛑 애플리케이션 종료 중... (PID: $PID)"
  kill -15 $PID
  sleep 5

  # 종료 확인
  if ps -p $PID > /dev/null; then
    echo "⛔ 강제 종료합니다..."
    kill -9 $PID
  else
    echo "✅ 정상 종료되었습니다."
  fi
fi

echo "🚀 애플리케이션 시작 중..."
nohup java -jar $JAR_PATH > $LOG_PATH 2>&1 &

sleep 3
NEW_PID=$(ps -ef | grep $APP_NAME | grep java | grep -v grep | awk '{print $2}')

if [ -z "$NEW_PID" ]; then
  echo "❌ 애플리케이션 실행 실패"
  exit 1
else
  echo "✅ 애플리케이션 실행 완료 (PID: $NEW_PID)"
fi
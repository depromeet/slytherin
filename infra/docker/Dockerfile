# Base image 선택 (Java 21)
FROM openjdk:21-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 Docker 이미지에 추가
COPY build/libs/*.jar app.jar

# 애플리케이션을 실행하기 위한 명령어 설정
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
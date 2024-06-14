FROM openjdk:17-slim

WORKDIR /app

# 确保复制正确的 jar 文件到 Docker 容器中
COPY build/libs/Gomoku-0.0.1-SNAPSHOT.jar /app/gomoku.jar

EXPOSE 8080

CMD ["java", "-jar", "gomoku.jar"]

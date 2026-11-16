# Etapa 1: compila o projeto com Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: imagem final, só com o JRE e o JAR
FROM eclipse-temurin:21-jre-alpine

ARG APP_VERSION=local
ARG APP_COMMIT=local
ENV APP_VERSION=${APP_VERSION} \
    APP_COMMIT=${APP_COMMIT}

WORKDIR /app
COPY --from=builder /build/target/app.jar /app/app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

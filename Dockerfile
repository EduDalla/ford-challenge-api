# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B package \
    && cp target/*.jar /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ENV PORT=8080
EXPOSE 8080

RUN groupadd --system app && useradd --system --gid app --home-dir /app app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

USER app

CMD ["java", "-jar", "/app/app.jar"]

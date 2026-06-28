FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Execução (Runtime)
FROM eclipse-temurin:21-jre
WORKDIR /app


COPY --from=build /app/target/*[0-9].jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xss512k", "-jar", "app.jar"]

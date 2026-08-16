# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/signalforge-1.0.0.jar app.jar

EXPOSE 1111

ENTRYPOINT ["java", "-jar", "app.jar"]
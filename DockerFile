FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

EXPOSE 1111

CMD ["java", "-jar", "target/utptimerobot-0.0.1-SNAPSHOT.jar"]
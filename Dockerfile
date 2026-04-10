FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY banking-system-backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY banking-system-backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/banking-system-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE ${PORT:-8080}
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]

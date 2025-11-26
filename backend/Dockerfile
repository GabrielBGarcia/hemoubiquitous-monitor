FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY src/main/resources/hemoubiquitous-fhir-firebase-adminsdk-fbsvc-42a25b9c4b.json ./src/main/resources/
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


# Build the Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Run the application using Java 17
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Keep Java memory usage suitable for a small demo
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:+UseSerialGC"

EXPOSE 10000

CMD ["java", "-jar", "app.jar"]

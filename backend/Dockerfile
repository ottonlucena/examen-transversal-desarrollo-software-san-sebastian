# Etapa 1: compilar con el Maven Wrapper del proyecto
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# Los tests usan Testcontainers (requieren Docker); se ejecutan fuera con ./mvnw verify
RUN ./mvnw -B -q package -DskipTests

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app && mkdir -p /app/uploads && chown app:app /app/uploads
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY .mvn ./.mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
COPY testng.xml .

ENTRYPOINT ["./mvnw", "clean", "verify", "-Dheadless=true"]

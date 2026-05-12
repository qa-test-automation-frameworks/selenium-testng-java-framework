FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

COPY .mvn ./.mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY config ./config
COPY src ./src
COPY testng.xml .

ENTRYPOINT ["./mvnw", "clean", "verify", "-Dheadless=true"]

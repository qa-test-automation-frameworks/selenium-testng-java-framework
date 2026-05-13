FROM eclipse-temurin:21.0.7_6-jdk@sha256:03a98128909d4216057841a9e779af84b3e395a62f412b3073b369a53c02465b

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

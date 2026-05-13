FROM eclipse-temurin:21.0.7_6-jdk@sha256:03a98128909d4216057841a9e779af84b3e395a62f412b3073b369a53c02465b

WORKDIR /app

RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --uid 10001 testuser

COPY --chown=testuser:testuser .mvn ./.mvn
COPY --chown=testuser:testuser mvnw .
COPY --chown=testuser:testuser pom.xml .
RUN chmod +x ./mvnw && chown -R testuser:testuser /app
USER testuser
RUN ./mvnw dependency:go-offline -B

COPY --chown=testuser:testuser config ./config
COPY --chown=testuser:testuser src ./src
COPY --chown=testuser:testuser testng.xml .

ENTRYPOINT ["./mvnw", "clean", "verify", "-Dheadless=true"]

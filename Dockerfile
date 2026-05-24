FROM eclipse-temurin:24.0.2_12-jdk@sha256:7493205ffe6caa8074fa8a06a276bb1c5ac41d3dd0fd43a0db66d7f776e80b3e

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

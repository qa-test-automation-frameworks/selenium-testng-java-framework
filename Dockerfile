# Use a base image with Maven, JDK 21, and Chrome installed
FROM markhobson/maven-chrome:jdk-21

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file first to leverage Docker layer caching
COPY pom.xml .

# Pre-download dependencies (optional but speeds up subsequent builds)
RUN mvn dependency:go-offline -B

# Copy the entire project source code
COPY src ./src
COPY testng.xml .

# Set environment variables if needed
ENV DISPLAY=:99

# Run tests by default
# We pass -Dheadless=true to ensure Chrome runs in headless mode in the container
ENTRYPOINT ["mvn", "clean", "verify", "-Dheadless=true"]

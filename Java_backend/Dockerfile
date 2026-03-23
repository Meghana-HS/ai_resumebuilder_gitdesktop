FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build app
RUN mvn clean package -DskipTests

# Run app (Render provides PORT)
CMD ["java", "-jar", "target/app-0.0.1-SNAPSHOT.jar"]
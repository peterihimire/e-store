# Build the jar with the same JDK the project targets.
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

# Resolve dependencies in a separate layer so source changes don't re-download them.
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# Spring Boot layered jar -> small, cache-friendly runtime layers.
FROM eclipse-temurin:17-jre AS extractor
WORKDIR /build
COPY --from=builder /workspace/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre
LABEL org.opencontainers.image.title="e-store" \
      org.opencontainers.image.description="E-Store REST API" \
      org.opencontainers.image.source="https://github.com/"

WORKDIR /app
COPY --from=extractor /build/dependencies/ ./
COPY --from=extractor /build/spring-boot-loader/ ./
COPY --from=extractor /build/snapshot-dependencies/ ./
COPY --from=extractor /build/application/ ./

RUN groupadd --system estore && \
    useradd --system --gid estore estore && \
    chown -R estore:estore /app
USER estore

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

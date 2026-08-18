# Runtime-only image: the jar is built beforehand with `./mvnw package`,
# because jOOQ code generation needs Docker (Testcontainers) - unavailable
# on the platform's image builders
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY target/task-management-*.jar app.jar

EXPOSE 8080
# MaxRAMPercentage: the default heap is a quarter of the container memory; 65 %
# of the 1 GB machine gives ~650 MB heap and leaves room for metaspace, code
# cache, thread stacks, and direct buffers. The Vaadin sessions live on this heap.
# ExitOnOutOfMemoryError: a heap OOM otherwise leaves a half-dead process that
# may keep answering the health check; exiting lets the platform restart it.
ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=65.0", \
    "-XX:InitialRAMPercentage=30.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-jar", "app.jar"]

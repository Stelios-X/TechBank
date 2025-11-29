# Multi-stage build for api-gateway
FROM openjdk:21-slim as builder
WORKDIR /workspace
ARG JAR_FILE=target/api-gateway-1.0.0.jar
COPY ${JAR_FILE} api-gateway.jar
RUN java -Djarmode=layertools -jar api-gateway.jar extract

FROM openjdk:21-slim
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /application
COPY --from=builder --chown=spring:spring /workspace/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
EXPOSE 8000

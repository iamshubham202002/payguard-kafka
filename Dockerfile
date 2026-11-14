# Build either service from the repo root:
#   docker build --build-arg MODULE=payment-service -t payguard/payment-service .
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /build
COPY . .
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
RUN useradd --system --uid 1001 appuser
COPY --from=build /build/${MODULE}/target/${MODULE}-*.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]

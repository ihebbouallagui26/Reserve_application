# syntax=docker/dockerfile:1
# Build context is Rezkna-Backend/ (not this directory) so the build can see common-lib.
# common-lib is installed to the local .m2 cache first, then this service builds against
# it as an ordinary Maven coordinate - no multi-module reactor needed inside the image.
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /app
COPY common-lib ./common-lib
RUN --mount=type=cache,target=/root/.m2,id=rezkna-maven-cache,sharing=locked \
    mvn -B -q -f common-lib/pom.xml -DskipTests install
COPY restaurant-service ./restaurant-service
RUN --mount=type=cache,target=/root/.m2,id=rezkna-maven-cache,sharing=locked \
    mvn -B -q -f restaurant-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/restaurant-service/target/*.jar app.jar
EXPOSE 4002
ENTRYPOINT ["java", "-jar", "app.jar"]

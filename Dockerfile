FROM gradle:8.10-jdk17 AS build
WORKDIR /
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle bootJar --build-cache

FROM eclipse-temurin:17-jdk

RUN apt-get update  \
    && apt-get install -y curl  \
    && rm -rf /var/lib/apt/lists/*  # for healthcheck via curl inside container

WORKDIR /app
ARG JAR_FILENAME=cloud-keeper.jar
ENV FILENAME=${JAR_FILENAME}
COPY --from=build /build/libs/${JAR_FILENAME} .
EXPOSE 8080
CMD java -jar ${FILENAME}
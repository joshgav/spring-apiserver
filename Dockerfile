FROM docker.io/library/gradle:latest

USER 0
WORKDIR /app-root

COPY gradle gradle
COPY build.gradle gradle.properties gradlew settings.gradle ./
COPY .s2i .s2i
COPY src src

RUN ./.s2i/bin/assemble

RUN cp .s2i/bin/run ./entrypoint.sh

ENTRYPOINT ./entrypoint.sh

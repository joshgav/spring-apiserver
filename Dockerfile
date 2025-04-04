FROM registry.access.redhat.com/ubi9/openjdk-21:latest

USER 0
WORKDIR /app-root

COPY build.gradle gradle.properties gradlew settings.gradle ./
# TODO: copy if exists
# COPY .gradle .gradle
COPY gradle gradle

COPY src src
COPY scripts scripts

RUN scripts/build.sh

ENTRYPOINT scripts/run.sh

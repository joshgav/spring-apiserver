#! /usr/bin/env bash

this_dir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
root_dir=$(cd ${this_dir}/.. && pwd)
if [[ -f ${root_dir}/.env ]]; then source ${root_dir}/.env; fi
if [[ -f ${this_dir}/.env ]]; then source ${this_dir}/.env; fi

echo "INFO: Building application bootable jar using gradle"

export GRADLE_OPTS="-Dorg.gradle.daemon=false"
${root_dir}/gradlew build -x test

# TODO this won't work if >1 jar is generated
echo "INFO: Copying built application to app-root..."
cp -a ${root_dir}/build/libs/*-SNAPSHOT.jar ${ROOT_DIR}/app.jar

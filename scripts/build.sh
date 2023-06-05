#! /usr/bin/env bash

this_dir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
root_dir=$(cd ${this_dir}/.. && pwd)
if [[ -f ${root_dir}/.env ]]; then source ${root_dir}/.env; fi
if [[ -f ${this_dir}/.env ]]; then source ${this_dir}/.env; fi

echo "this_dir: ${this_dir}"
echo "root_dir: ${root_dir}"

echo "INFO: Building application bootable jar using gradle"

export GRADLE_OPTS="-Dorg.gradle.daemon=false"
${root_dir}/gradlew build -x test

# TODO this won't work if >1 jar is generated
echo "INFO: Copying built application to root dir..."
cp -a ${root_dir}/build/libs/*-SNAPSHOT.jar ${root_dir}/app.jar

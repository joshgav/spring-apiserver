#! /usr/bin/env bash

this_dir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
root_dir=$(cd ${this_dir}/.. && pwd)

echo "INFO: pwd: ${this_dir}"
echo "INFO: root dir: ${root_dir}"

if [[ -v PG_username ]]; then
    echo "INFO: Setting binding metadata from env vars"

    export SPRING_APPLICATION_JSON="{
        \"spring\": {
            \"datasource\": {
                \"username\": \"${PG_username}\",
                \"password\": \"${PG_password}\",
                \"url\": \"jdbc:postgresql://${PG_host}:${PG_port}/${PG_dbname}\"
            }
        }
    }"
fi

echo "DEBUG: SPRING_APPLICATION_JSON: ${SPRING_APPLICATION_JSON}"

exec java \
    -Dspring.config.additional-location=optional:file:/opt/config/application.yaml \
    -jar ${root_dir}/app.jar

#! /usr/bin/env bash
root_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
app_name=${1:-apiserver}

if ! which pg_ctl > /dev/null; then echo "ERROR: please install postgres"; exit 1; fi

# helpful manual commands
# stop server: `pg_ctl stop -D temp/pgsql/data`
# connect to db: `PGPASSWORD=$(cat temp/pgsql/pwfile) psql -h localhost -p 5432 -U apiserver postgres`

# set up local server in repo-relative path
pgsql_dir=${root_dir}/temp/pgsql
data_dir=${pgsql_dir}/data
# local password persisted in file for later retrieval and for `initdb` discovery
# this is ok cause local DB has no real data and is only accessible within the local system (i.e. at localhost)
pgsql_pwfile=${pgsql_dir}/pwfile

# standard postgres env var to save us from having to explicitly specify params below
export PGDATA=${data_dir}

# make it easy to reset the DB - just set DELETE_EXISTING_DB=1
if [[ -n "${DELETE_EXISTING_DB}" ]]; then
    pg_ctl status &> /dev/null
    result_code=$?
    if [[ ${result_code} == 0 ]]; then
        echo "stopping server"
        pg_ctl stop
        if [[ $? != 0 ]]; then
            echo "ERROR: failed to stop server"
            exit 1
        fi
    else
        echo "server not running"
    fi
    echo "deleting data dir"
    rm -rf ${pgsql_dir}
    if [[ $? != 0 ]]; then
        echo "ERROR: failed to delete pgsql dir ${pgsql_dir}"
        exit 1
    fi
fi
mkdir -p ${pgsql_dir}

# initialize first user and password
username=${app_name}
password=
if [[ -e ${pgsql_pwfile} ]]; then
    password=$(cat ${pgsql_pwfile})
else
    password=$(openssl rand -hex 16 | tee ${pgsql_pwfile})
fi
echo -e "local db:\n\tusername: ${username}\n\tpassword: ${password}"

# standard Postgres env vars to save typing params
export PGPASSWORD=${password}

# run initdb if database doesn't yet exist
if [[ ! -d ${data_dir} ]]; then
    echo "initdb at ${data_dir}"
    pg_ctl initdb --pgdata=${data_dir} \
        --options="--auth=md5 --username=${username} --encoding=utf8 --pwfile=${pgsql_pwfile}"
    if [[ $? != 0 ]]; then
        echo "initdb failed"
        exit 1
    else
        echo "initdb succeeded"
    fi
else
    echo "using existing db at ${data_dir}"
fi

# ensure server is running
pg_ctl status &> /dev/null
result_code=$?
if [[ ${result_code} == 3 || ${result_code} == 1 ]]; then
    # 3 means server is stopped (https://www.postgresql.org/docs/13/app-pg-ctl.html)
    echo "starting server..."
    pg_ctl start --log=${pgsql_dir}/pgsql-$(date '+%Y%m%dT%H%M%S').log
elif [[ ${result_code} == 4 ]]; then
    echo "ERROR: data dir ${data_dir} not found"
    exit 1
elif [[ ${result_code} == 0 ]]; then
    echo "server already running"
else
    echo "ERROR: unexpected error checking server status"
    exit 1
fi
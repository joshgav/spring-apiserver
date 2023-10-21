# apiserver

A basic Spring Web API server over a PostgresQL DB for tests and experiments.

- to build and test: `./gradlew build`

## Init a database

- install postgres (e.g. `brew install postgres`, `apt install postgresql`)
- add yourself to postgres group: `sudo usermod -aG postgres ${USER}`
    - logout and back in
- run `./deploy/run-local-db.sh`
    - check logs for errors and fix them
    - you may need to `chmod -R 0775 /var/run/postgresql`
- set password in [`src/main/resources/application.yaml`](./src/main/resources/application.yaml) to match password in `./temp/pgsql/pwfile`
- run `./gradlew bootRun`
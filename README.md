# Bonita Update Tool

The Bonita Update Tool updates an installed Bonita Community platform from one version to another by running the
database migrations of every intermediate version in sequence. Supported source and target versions are listed in
[`bonita.versions`](bonita.versions).

For end-user instructions (configuration and how to run the tool), see the README shipped in the distribution,
[`src/main/dist/README.md`](src/main/dist/README.md), and the
[Bonita documentation](https://documentation.ofelia.com/bonita/latest/version-update/update-with-update-tool).


## Requirements

* A locally installed JDK 17. The build selects it through Gradle toolchain auto-detection; toolchain
  auto-provisioning is not configured, so Gradle does not download it.
* Docker, to run the integration and update tests against a database started by the build
  (see [Database used by the tests](#database-used-by-the-tests)).


## Build

```bash
./gradlew build
```

The build runs the unit tests and produces the distribution zip in `build/distributions/`.


## Tests

Tests are written with the [Spock framework](https://spockframework.org/) and live in four source sets.

| Source set        | Directory                    | Needs a database | Purpose                                                            |
|-------------------|------------------------------|------------------|--------------------------------------------------------------------|
| `test`            | `src/test/groovy`            | no               | Unit tests of the steps orchestration, warnings and helpers.       |
| `integrationTest` | `src/integrationTest/groovy` | yes              | Database operations of the steps and of `DatabaseHelper`.          |
| `filler`          | `src/filler/groovy`          | yes              | Populate a Bonita database in version n-1 before an update test.   |
| `enginetest`      | `src/enginetest/groovy`      | yes              | Check the Bonita Engine behaves in version n after an update test. |

### Unit tests

```bash
./gradlew test
```

### Integration tests

```bash
./gradlew integrationTest
```

### Update tests

Update tests are end-to-end tests. For each version step of [`bonita.versions`](bonita.versions), that is for each
version except the first one, the build:

1. creates an empty Bonita database in version n-1,
2. starts a Bonita Engine in version n-1 and fills it with data through the `filler` classes,
3. stops the engine and runs the update tool to version n,
4. starts a Bonita Engine in version n and runs the `enginetest` classes of that version.

```bash
./gradlew allUpdateTests          # every version step
./gradlew lastUpdateTests         # last version step only
./gradlew testUpdate_10_4_0       # one version step
```

The task of a version step is named after the target version with dots replaced by underscores, and the major version
padded to two digits so that the tasks sort in order: `testUpdate_07_14_0`, `testUpdate_10_3_0`, `testUpdate_11_0_0`.
Run `./gradlew tasks --all | grep testUpdate_` to list them.

The update phase of an update test logs at `INFO` level by default. Pass the `logger.level` system property to change
it:

```bash
./gradlew allUpdateTests -Dlogger.level=DEBUG
```


## Database used by the tests

Integration and update tests need a database. The vendor is selected with the `db.vendor` system property, one of
`postgres` (default), `mysql`, `oracle` or `sqlserver`.

The build always starts a Docker container for the selected vendor: it pulls the vendor image, starts the container,
runs the tests and removes the container. Passing a `db.url` system property to Gradle has no effect, the container
URL and credentials are used. **The build drops and recreates the test database and its user at the start of each
update test.**

```bash
./gradlew integrationTest allUpdateTests -Ddb.vendor=postgres
```

Images, default ports and credentials are defined in
[`DockerDatabaseContainerTasksCreator`](buildSrc/src/main/groovy/org/bonitasoft/update/plugin/db/DockerDatabaseContainerTasksCreator.groovy).
The Oracle image is hosted on a private registry: export `DOCKER_BONITASOFT_REGISTRY`, `REGISTRY_USERNAME` and
`REGISTRY_TOKEN` before running the build.

### Run integration tests from the IDE

The IDE does not start a database container. Start a database yourself, with the database and user already created,
and pass the following system properties to the test run configuration. The tests read them directly and create and
drop their own tables in that database.

| System property  | Purpose                                                                            |
|------------------|------------------------------------------------------------------------------------|
| `db.vendor`      | `postgres`, `mysql`, `oracle` or `sqlserver`                                       |
| `db.url`         | JDBC URL of the test database                                                      |
| `db.driverClass` | JDBC driver class                                                                  |
| `db.user`        | User owning the test database                                                      |
| `db.password`    | Password of that user                                                              |

For example, with a local PostgreSQL holding a `bonita` database owned by user `bonita`:

```
-Ddb.vendor=postgres
-Ddb.url=jdbc:postgresql://localhost:5432/bonita
-Ddb.driverClass=org.postgresql.Driver
-Ddb.user=bonita
-Ddb.password=bpm
```


## Debugging

The following settings start the corresponding JVM suspended, listening for a remote debugger on port 5005:

| Setting          | Debugs                             |
|------------------|------------------------------------|
| `-Dfiller.debug` | the filler phase of an update test |
| `-Dupdate.debug` | the update phase of an update test |
| `--debug-jvm`    | the tests themselves               |

To debug the build scripts, export the debug options before launching Gradle:

```bash
export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

To inspect the tasks a test triggers, add `taskTree` to the command, for instance
`./gradlew testUpdate_10_4_0 taskTree -Ddb.vendor=postgres`.


## License

The Bonita Update Tool Community edition is released under the [GNU LGPL 2.1](LICENSE).

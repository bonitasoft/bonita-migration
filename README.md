bonita-migration
=================

What it does?
-------------
This project migrates an installed Bonita community instance from one version to another.


Build migration
---------------
```
./gradlew build
```


Integration tests
-----------------
These tests need a database to run. They ensure that migration database operations are performed correctly.

```
./gradlew integrationTest
```


Migration tests
---------------
These tests involve preloaded data, migration and ensure that the Bonita Engine works correctly after migration.

For each supported version, the following is performed
* initialized an empty Bonita database for version n-1
* run a Bonita Engine in version n-1 and use to it fill data (using filler classes)
* stop the engine and run the migration process to version n
* run a Bonita Engine in version n and run dedicated tests for this version n (using enginetest classes)

### Run all tests

```
./gradlew allMigrationTests
```

### Test a specific version

```
./gradlew testMigration_X_Y_Z
```


Customize database to use for integration and migration tests when running with Gradle
--------------------------------------------------------------------------------------

Tests can be run on all supported databases. To select the database type, pass the `db.vendor` value as System Property
to the gradle command. By default the database used is `postgres`.

### Rely on external database

You have to pass several System Properties to the Gradle command as described in the example below. Default values for the
properties are set in the [DatabaseResourcesConfigurator](buildSrc/src/main/groovy/org/bonitasoft/migration/plugin/db/DatabaseResourcesConfigurator.groovy)
source file.
Some other settings are also available in [DockerDatabaseContainerTasksCreator](buildSrc/src/main/groovy/org/bonitasoft/migration/plugin/db/DockerDatabaseContainerTasksCreator.groovy)

**Important**:
* the build will remove the database and user after the tests complete, so all previous data stored in the
database are lost
* if the database and the user does not exist prior to the build start, they will be created so you don't have to manage
this by yourself. Only the root user must exist and should be able to connect to the database server
* the `db.url` for `sqlserver` must use the `jdbc:sqlserver://<host_or_ip>:<port>;database=<db_name>[optional extra parameters]`
 syntax as it is parsed to be able to create the targeted database


Example: run test migration 7.6.0 on Sqlserver:
```
./gradlew testMigration_7_6_0 \
-Ddb.vendor=sqlserver \
-Ddb.url=jdbc:sqlserver://myhost:1433;database=migration \
-Ddb.root.user=sa \
-Ddb.root.password=StrongPassword \
-Ddb.user=migration_ci \
-Ddb.password=migration_ci \
-Ddb.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

Example: run integration tests on Postgresql:
```
./gradlew integrationTest \
-Ddb.vendor=postgres \
-Ddb.url=jdbc:postgresql://localhost:5432/bonita \
-Ddb.root.user=postgres \
-Ddb.root.password=postgres \
-Ddb.user=bonita \
-Ddb.password=bpm \
-Ddb.driverClass=org.postgresql.Driver
```

Example: run all tests on Oracle:
```
./gradlew clean integrationTest allMigrationTests --info --stacktrace \
-Ddb.vendor=oracle \
-Ddb.url=jdbc:oracle:thin:@//localhost:1521/ORCLPDB1.localdomain \
-Ddb.root.user="sys as sysdba" \
-Ddb.root.password=oracle \
-Ddb.user=bonita \
-Ddb.password=bpm \
-Ddb.driverClass=oracle.jdbc.OracleDriver
```

Example: run all tests on Mysql:
```
./gradlew clean integrationTest allMigrationTests --info --stacktrace \
-Ddb.vendor=mysql \
-Ddb.url=jdbc:mysql://localhost:3306/bonita?allowMultiQueries=true \
-Ddb.root.user=root \
-Ddb.root.password=root \
-Ddb.user=bonita \
-Ddb.password=bpm \
-Ddb.driverClass=com.mysql.jdbc.Driver
```


### Let the build run docker database containers

The build can start the related database container for you. To do so, only pass the `db.vendor` System Property.
If you provide a `db.url` System Property in addition, the build won't start the database container and will expect you to run an external database.

Example: run all integration and migration tests on Oracle:
```
./gradlew clean integrationTest allMigrationTests -Ddb.vendor=oracle
```


Customize database to use for integration tests in IDE
------------------------------------------------------

In that case, you must have a running external database and pass the corresponding System Properties to the test class as this is required
when running the Gradle build.


Debug
-----
When activating one of the following settings, the process is started suspended and listening on port 5005. Attach a remote
debugging system to resume and debug the process
* use `-Dfiller.debug` to debug the filler phase of database filler
* use `-Dmigration.debug` to debug the migration
* use `--debug-jvm` to debug the tests
* use `export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"` before launching the build in order to debug the build script itself


Publication
-----------
```
./gradlew publishToMavenLocal
```

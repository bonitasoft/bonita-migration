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


Customize database to use
--------------------------
By default the database used is 
```
dbvendor = postgres
dburl = jdbc:postgresql://localhost:5432/migration
dbuser = bonita
dbpassword = bpm
dbdriverClass = org.postgresql.Driver
dbRootUser = postgres
dbRootPassword = postgres
```

Connection parameters can be customized using system property

Example: run test migration 7.6.0 on a sqlserver:
```
./gradlew clean integrationTest allMigrationTests \
-Ddb.vendor=sqlserver \
"-Ddb.url=jdbc:sqlserver://sqlserver2.rd.lan:1433;database=mig_pr" \
-Ddb.user=migration_ci -Ddb.password=migration_ci -Ddb.root.user=sa \
-Ddb.root.password=Bonita12 \
-Ddb.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

Example: run all tests on Oracle (in a locally running docker):
```
./gradlew clean integrationTest allMigrationTests --info --stacktrace \
-Ddb.vendor=oracle \
-Ddb.url=jdbc:oracle:thin:@localhost:1521:xe \
-Ddb.root.user="sys as sysdba" \
-Ddb.root.password=oracle \
-Ddb.driverClass=oracle.jdbc.OracleDriver \
-Djava.security.egd=file:/dev/../dev/urandom \
-Dauto.accept=true
```

Example: run all tests on Mysql (in a locally running docker):
```
./gradlew clean integrationTest allMigrationTests --info --stacktrace \
-Ddb.vendor=mysql \
"-Ddb.url=jdbc:mysql://localhost:3306/bonita?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8" \
-Ddb.root.user=root \
-Ddb.root.password=root \
-Ddb.driverClass=com.mysql.jdbc.Driver \
```


Publication
-----------
```
./gradlew publishToMavenLocal
```


Debug
-----
When activating one of the following settings, the process is started suspended and listening on port 5005. Attach a remote
debugging system to resume and debug the process
* use `-Dfiller.debug` to debug the filler phase of database filler
* use `-Dmigration.debug` to debug the migration
* use `--debug-jvm` to debug the tests
* use `export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"` before launching the build in order to debug the build script itself

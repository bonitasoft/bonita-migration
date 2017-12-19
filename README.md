bonita-migration
=================

What it does?
-------------
This project migrates an installed Bonita community instance from one version to another.


Build migration
---------------
```
./gradlew.sh build
```

Integration tests
-----------------
```
./gradlew.sh integrationTest allMigrationTests
```

Test specific version
-----------------
```
./gradlew.sh testMigration_X_Y_Z
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

Publication
-----------
```
./gradlew.sh publishToMavenLocal
```


Debug
-----
* use `-Dfiller.debug` to debug the filler phase of database filler
* use `-Dmigration.debug` to debug the migration
* use `--debug-jvm` to debug the tests
* use `export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"` before launching the build in order to debug the build script itself

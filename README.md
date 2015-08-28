bonita-migration
=================

What it does?
-------------
this project migrate an installed bonita open solution from one version to another


Dev setup
---------
Setup
```
curl -s get.gvmtool.net | bash
gvm install groovy 2.4.3
gvm install gradle 2.4
```
Check
```
gvm current
Using:
gradle: 2.4
groovy: 2.4.3
```

Speed build with gradle daemon 
https://docs.gradle.org/2.4/userguide/gradle_daemon.html

* Windows:
```
(if not exist "%HOMEPATH%/.gradle" mkdir "%HOMEPATH%/.gradle") && (echo foo >> "%HOMEPATH%/.gradle/gradle.properties")
```
* Unix:
```
touch ~/.gradle/gradle.properties && echo "org.gradle.daemon=true" >> ~/.gradle/gradle.properties
```

Projects
--------
* bonita-migration-plugins
* bonita-migration
* bonita-migration-sp

Build plugins
-------------
```
cd bonita-migration-plugins
gradle install
```

Build migration
---------------
```
cd bonita-migration
gradle testMigration
```

Build migration SP
---------------
```
cd bonita-migration-sp
gradle testMigration
```
Debug
-----
* use `gradle clean install testMigration -Dfiller.debug` to debug the filler phase of database filler
* use `gradle clean install testMigration -Dmigration.debug` to debug the filler phase of database filler
* use `gradle clean install testMigration test --debug-jvm` to debug the filler phase of database filler
* use `export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"` before launching the build in order to debug the build script itself

How to tag
----------

* in the build.gradle remove in the overrided version list the SNAPSHOT version
* remove all others SNAPSHOT version from the bonita versions
* keep at least [:] inside overrided versions


Add a migration step
--------------------

* add in the build.gradle the new version
* add this version in the overrided versions like this `[7.X.Y:7.X.Y-SNAPSHOT]`
* add a new folder `migrateTo_7_X_Y` containing the filler and tests like in other steps
* add a new Migration step in bonita-migration-distrib (class must be `org.bonitasoft.migration.version.to7_X_Y.MigrateTo7_X_Y`)

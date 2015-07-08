FAQ bonita migration v2
=======================

Tools
-----
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

Other tasks
-----------
```
gradle tasks
```

Create a new migration step
===========================

TODO


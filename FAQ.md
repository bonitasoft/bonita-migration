FAQ bonita-migration
====================

Tools
-----
Setup
```
curl -s get.gvmtool.net | bash
gvm install groovy 2.4.3
gvm install gradle 2.4
```
check
```
gvm current
Using:
gradle: 2.4
groovy: 2.4.3
```

tips: enable speed build with gradle daemon 
https://docs.gradle.org/2.4/userguide/gradle_daemon.html

Windows:
```
(if not exist "%HOMEPATH%/.gradle" mkdir "%HOMEPATH%/.gradle") && (echo foo >> "%HOMEPATH%/.gradle/gradle.properties")
```
Unix:
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

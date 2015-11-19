Bonita migration tool
=======================

Configuration
--------------

First change the configuration of the tool located in **Config.properties**


###Parameters required
>     bonita.home <path to bonita home>
>     db.vendor <the kind of your database>, can be [mysql,postgres,sqlserver,oracle]
>     db.url <the url of the database>
>     db.driverclass <the class of the jdbc driver>
>     db.user <the username to connect to the database>
>     db.password <the password to connect to the database>

###Optional parameters
>     target.version <the version your installation will be in>

by default the target.version is the last one available

###Additional system properties 
to be set with -Dkey=value
>     auto.accept <true|false> will answer yes to every questions, default = false

All parameters can be overridden with system properties

### Database configuration
* For MySql : use allowMultiQueries=true in db url

Also note that the jdbc driver must be put in the lib folder. Create "lib" folder in root of project.

Run the migration
-----------------
just run the **bonita-migration-distrib** or **bonita-migration-distrib.bat**

example:
>     ./bonita-migration-distrib

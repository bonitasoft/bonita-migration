
bonita-migration
=================
What it does?
-------------
this project migrate an installed bonita open solution from one version to an other
Run it
------
Required parameters:
>     --bonita.home <path to bonita home>
>     --source.version <the current version of your installation>     -> not used yet
>     --target.version <the version your installation will be in>     -> not used yet
>     --db.vendor <the kind on you database, can be [mysql,postgres,sqlserver,oracle]
>     --db.url <the url of the database>
>     --db.driverclass <the class of the jdbc driver>
>     --db.user <the username to connect to the database>
>     --db.password <the password to connect to the database>

For MySql : use allowMultiQueries=true in db url
For Oracle :  In sql scripts, don't use ";" after each request, but "@@"
For SQLServer :  In sql scripts, don't use "GO" after each request, but "@@"

also not that the jdbc driver must be put in the lib folder

it launches all scripts inside the versions folder


example:
>     groovy Migration.groovy --bonita.home /home/user/bonita.home --source.version 6.0.3 --target.version 6.1.0 --db.vendor postgres --db.url jdbc:postgresql://localhost:5432/bonita --db.driverclass org.postgresql.Driver --db.user bonita --db.password bonita

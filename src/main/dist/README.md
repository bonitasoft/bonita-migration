Bonita Migration Tool
=======================

**Bonita Migration Tool** upgrades a Bonita installation to a more recent version.

# Pre-requisites

Make sure you fill all pre-requisites (like making a backup of your database) by reading the
[online documentation](http://documentation.bonitasoft.com/?page=migrate-from-an-earlier-version-of-bonita-bpm).

Also note that the jdbc driver file (.jar or .zip) must be placed in the lib/ folder.


# Configuration

Configuration can be done by changing file **Config.properties**, or by setting specific command line parameters.


## Configuration using file Config.properties

Edit file **Config.properties** and changed the provided default values.


## Configuration using command line parameters

export a System variable named `BONITA_MIGRATION_DISTRIB_OPTS` and give it the value `"-Dkey1=value1 -Dkey2=value2"`  
Eg.  
`export BONITA_MIGRATION_DISTRIB_OPTS="-Dtarget.version=7.5.0 -Dauto.accept=true -Ddb.vendor=postgres"`


## Accepted parameters

### Mandatory parameters
>     db.vendor <the kind of your database>, can be [mysql,postgres,sqlserver,oracle]
>     db.url <the url of the database>
>     db.driverclass <the class of the jdbc driver>
>     db.user <the username to connect to the database>
>     db.password <the password to connect to the database>

### Optional parameters
>     target.version <the version your installation will be in>. If not specified, target version will be prompted interactively.
>     auto.accept <true|false> will answer yes/no to every questions, without prompting for confirmation. Default = false.

By default the migration tool will propose you to migrate to the last Bonita version available.


### Database configuration
* For MySql : add `allowMultiQueries=true` in db url


# Run the migration

Once the configuration done, you can run the migration:  
just run the **bonita-migration-distrib** or **bonita-migration-distrib.bat** script.

example (Unix):
>    ./bonita-migration-distrib

example (Windows):
>    bonita-migration-distrib.bat

# Verify the migration can be executed

The migration script already checks before running that the migration can be executed. However if you
only want to verify the migration can be executed without actually executing it, you can use the scipt
called `check-migration-dryrun`. It will only run the checks. You can also run the normal script with the `--verify`
option to achieve the same goal.

example (Unix):
>    ./check-migration-dryrun

example (Windows):
>    check-migration-dryrun.bat


# Extra tools

In the `tools` folder, you will find extra tools.

## Live migration

If you migrated through version 7.7.0 of bonita using the migration tool version 2.41.1 or greater, the table `arch_contract_data`
was backed up to avoid long running migration. You will need to run this tool after the migration was done in order to reintegrate
data from the `arch_contract_data_backup` table.
Follow instructions in the README.md of this tool.

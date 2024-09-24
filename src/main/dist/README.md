Bonita Update tool
=======================

**Bonita Update tool** upgrades a Bonita installation to a more recent version.

# Pre-requisites

Make sure you fill all pre-requisites (like making a backup of your database) by reading the
[online documentation](https://documentation.bonitasoft.com/bonita/latest/version-update/migrate-from-an-earlier-version-of-bonita).

Also note that the jdbc driver file (.jar or .zip) must be placed in the lib/ folder.


# Configuration

Configuration can be done by changing file **Config.properties**, or by setting specific command line parameters.


## Configuration using file Config.properties

Edit file **Config.properties** and changed the provided default values.


## Configuration using command line parameters

export a System variable named `BONITA_UPDATE_TOOL_OPTS` and give it the value `"-Dkey1=value1 -Dkey2=value2"`  
Eg.  
`export BONITA_UPDATE_TOOL_OPTS="-Dtarget.version=7.14.0 -Dauto.accept=true -Ddb.vendor=postgres"`


## Accepted parameters

### Mandatory parameters
>     db.vendor <the kind of your database>, can be [mysql,postgres,sqlserver,oracle]. Note that from version 10.2.0, Bonita Community edition only supports PostgreSQL database.
>     db.url <the url of the database>
>     db.driverclass <the class of the jdbc driver>
>     db.user <the username to connect to the database>
>     db.password <the password to connect to the database>

### Optional parameters
>     target.version <the version your installation will be in>. If not specified, target version will be prompted interactively.
>     auto.accept <true|false> will answer yes/no to every questions, without prompting for confirmation. Default = false.

By default, the update tool will propose you to update to the last Bonita version available.


### Database configuration
* For MySql : add `allowMultiQueries=true` in db url


# Run the update

Once the configuration done, you can run the update:  
just run the **bonita-update-tool** or **bonita-update-tool.bat** script.

example (Unix):
>    ./bonita-update-tool

example (Windows):
>    bonita-update-tool.bat

# Verify the update can be executed

The update script already checks before running that the update can be executed. However, if you
only want to verify the update can be executed without actually executing it, you can use the script
called `check-update-dryrun`. It will only run the checks. You can also run the normal script with the `--verify`
option to achieve the same goal.

example (Unix):
>    ./check-update-dryrun

example (Windows):
>    check-update-dryrun.bat
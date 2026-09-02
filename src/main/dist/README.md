# Bonita Update Tool

The Bonita Update Tool updates an installed Bonita platform to a more recent version. It applies, in order, the
database changes of every Bonita version between the installed one and the target one.


## Before you start

Read the [update procedure](https://documentation.ofelia.com/bonita/latest/version-update/update-with-update-tool) in
the Bonita documentation. In particular, back up your database and stop the Bonita platform before running the tool.

The tool needs Java 17 or later, found through `JAVA_HOME` or the `PATH`.

The JDBC drivers for PostgreSQL, MySQL, Oracle and SQL Server are shipped in the `lib/` folder. To use another driver
version, replace the corresponding jar in that folder.


## Content of the distribution

| Path                              | Purpose                                                               |
|-----------------------------------|-----------------------------------------------------------------------|
| `bin/bonita-update`, `.bat`       | Runs the update                                                       |
| `bin/check-update-dryrun`, `.bat` | Only checks that the update can run, without changing anything        |
| `Config.properties`               | Configuration of the tool, mainly the database connection             |
| `lib/`                            | The tool and its dependencies, including the JDBC drivers             |
| `LICENSES/`                       | Third-party licenses                                                  |


## Configuration

Edit `Config.properties`, or pass the same properties as Java system properties through the `BONITA_UPDATE_OPTS`
environment variable. A system property overrides the value of the file. At startup the tool logs each property it
uses and where it comes from, with passwords hidden.

```shell
export BONITA_UPDATE_OPTS="-Dtarget.version=10.4.0 -Dauto.accept=true"
```

| Property               | Required | Description                                                                                                                     |
|------------------------|----------|---------------------------------------------------------------------------------------------------------------------------------|
| `db.vendor`            | yes      | `postgres`, `mysql`, `oracle` or `sqlserver`. From Bonita 10.2.0, the Community edition only supports PostgreSQL.              |
| `db.url`               | yes      | JDBC URL of the Bonita database. See the notes below.                                                                          |
| `db.driverClass`       | yes      | JDBC driver class. Examples for each vendor are given in `Config.properties`.                                                   |
| `db.user`              | yes      | Database user of the Bonita platform.                                                                                           |
| `db.password`          | yes      | Password of that user.                                                                                                          |
| `target.version`       | no       | Bonita version to update to. When not set, the tool lists the available versions and asks in the console.                      |
| `logger.level`         | no       | `ERROR`, `WARN`, `INFO` (default) or `DEBUG`.                                                                                   |
| `db.pool.size.initial` | no       | Initial size of the database connection pool. Default `3`.                                                                      |
| `db.pool.size.max`     | no       | Maximum size of the database connection pool. Default `10`.                                                                     |
| `auto.accept`          | no       | `true` answers yes to every confirmation. System property only, it is ignored in `Config.properties`. Default `false`.          |

Since Bonita 7.11.0, maintenance releases need no database update, so the available target versions are the first
release of each minor version: choose `10.4.0` to update to any `10.4.x`.

### Database URL

**PostgreSQL.** The tool looks for the Bonita tables in the schemas of the connection `search_path`. When the Bonita
tables live in a schema other than the default one of the database user, add the same `currentSchema` parameter as in
the Bonita platform configuration:

```
db.url=jdbc:postgresql://localhost:5432/bonita?currentSchema=bonita
```

The schemas actually searched are logged at startup under `Database Information`, as `search_path schemas`. Check
this line when the tool reports that a table does not exist.

**MySQL.** The URL must contain `allowMultiQueries=true`:

```
db.url=jdbc:mysql://localhost:3306/bonita?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8
```

**SQL Server.** Use the `jdbc:sqlserver://<host>:<port>;database=<name>` syntax.

**Oracle.** Use the service name syntax, for instance `jdbc:oracle:thin:@//localhost:1521/FREEPDB1.localdomain`.


## Run the update

Run the script from the `bin/` folder: the tool reads `../Config.properties` and writes its log file in the current
directory.

```shell
cd bin
./bonita-update
```

```bat
cd bin
bonita-update.bat
```

The tool checks that the installed platform can be updated and displays the update steps. When some steps have
pre-requisites, it displays them and asks for confirmation before starting, unless `auto.accept=true`. Everything is
also written to a `bonita-update-<date>.log` file in the current directory. Keep this file when contacting the support.


## Check the update without running it

The update script performs its checks before changing anything. To run only these checks, use the `--verify` option
or the `check-update-dryrun` script, which does the same:

```shell
./bonita-update --verify
./check-update-dryrun
```


## Other options

| Option                    | Effect                                                                                                                   |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------|
| `--verify`                | Only check that the platform can be updated to the target version.                                                       |
| `-i`, `--update-indexes`  | Only update the indexes to remove the tenant id from the indexed columns. Bonita 9.0 and later.                          |
| `-t`, `--create-lo-trigger` | Only create the PostgreSQL trigger that deletes large objects when rows are deleted from `temporary_content`.           |
| `-h`, `--help`            | Print the available options.                                                                                             |

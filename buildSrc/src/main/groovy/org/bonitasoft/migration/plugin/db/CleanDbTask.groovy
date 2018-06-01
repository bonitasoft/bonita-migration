/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.migration.plugin.db

import groovy.sql.Sql
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author Baptiste Mesta
 */
class CleanDbTask extends DefaultTask {

    private boolean dropOnly = false
    void setDropOnly(boolean dropOnly) {
        this.dropOnly = dropOnly
    }

    @Override
    String getDescription() {
        return "Drop and recreate the database in order to launch test in a clean one."
    }

    @TaskAction
    def cleanDb() {
        DatabasePluginExtension properties = project.extensions.getByType(DatabasePluginExtension.class)
        logger.quiet "Clean database for vendor ${properties.dbvendor}"
        logger.quiet "Drop only (no db and credentials creation)? ${dropOnly}"

        def drivers =  project.files(project.getConfigurations().getByName("drivers"))
        List<URL> urls = new ArrayList<>()
        drivers.each { File file ->
            urls.add(file.toURI().toURL())
        }
        //workaround classpath issue
        def loader1 = Sql.class.getClassLoader()
        drivers.each { File file ->
            loader1.addURL(file.toURI().toURL())
        }
        Sql.class.getClassLoader().loadClass(properties.dbdriverClass)

        switch (properties.dbvendor) {
            case "oracle":
                cleanOracleDb(properties)
                break
            case "sqlserver":
                cleanSqlServerDb(properties)
                break
            case "postgres":
                cleanPostgresDb(properties)
                break
            case "mysql":
                cleanMysqlDb(properties)
                break
        }
    }

    private List extractDataBaseNameAndGenericUrl(DatabasePluginExtension properties) {
        DbParser.DbConnectionSettings dbConnectionSettings = new DbParser().extractDbConnectionSettings(properties.dburl)

        def genericUrl = dbConnectionSettings.genericUrl
        def databaseName = dbConnectionSettings.databaseName
        logger.info "Will use database $databaseName with generic url $genericUrl"
        [databaseName, genericUrl]
    }

    private void cleanMysqlDb(DatabasePluginExtension properties) {
        checkRootCredentials(properties)
        def (databaseName, genericUrl) = extractDataBaseNameAndGenericUrl(properties)

        Sql sql = newSqlInstance(genericUrl, properties.dbRootUser, properties.dbRootPassword, properties.dbdriverClass)
        sql.executeUpdate("DROP DATABASE IF EXISTS " + databaseName)
        sql.eachRow("SELECT DISTINCT user FROM mysql.user WHERE user ='" + properties.dbuser + "'") {
            sql.executeUpdate("DROP USER " + properties.dbuser)
        }

        if (!dropOnly) {
            sql.executeUpdate("CREATE USER " + properties.dbuser + " IDENTIFIED BY '" + properties.dbpassword + "'")
            sql.executeUpdate("CREATE DATABASE " + databaseName + " DEFAULT CHARACTER SET utf8")
            sql.executeUpdate("GRANT ALL ON " + databaseName + ".* TO " + properties.dbuser)
        }
        sql.close()
    }

    private static Sql newSqlInstance(String url, String user, String password, String driverClassName) {
        Sql sql= Sql.newInstance(url, user, password, driverClassName)
        setQueryTimeout(sql)
        sql
    }

    private static Sql newSqlInstance(String url, Properties properties, String driverClassName) {
        Sql sql = Sql.newInstance(url, properties, driverClassName)
        setQueryTimeout(sql)
        sql
    }

    // avoid making db operations make the build freeze
    private static void setQueryTimeout(Sql sql) {
        sql.withStatement {
            stmt -> stmt.queryTimeout = 60 // in seconds
        }
    }

    private void cleanPostgresDb(DatabasePluginExtension properties) {
        checkRootCredentials(properties)
        def (databaseName, genericUrl) = extractDataBaseNameAndGenericUrl(properties)
        Sql sql = newSqlInstance((String) genericUrl, properties.dbRootUser, properties.dbRootPassword, properties.dbdriverClass)

        // postgres 9.3 script version
        sql.eachRow("""
                    SELECT pid
                    FROM pg_stat_activity
                    WHERE upper(pg_stat_activity.datname) = upper('$databaseName')
                      AND pid <> pg_backend_pid()
                    """ as String) {
            logger.info("disconnect connection id $it.pid from database $databaseName")
            sql.execute("""
                    SELECT pg_terminate_backend(pg_stat_activity.pid)
                    FROM pg_stat_activity
                    WHERE pg_stat_activity.datname = '$databaseName'
                    AND pid = $it.pid
                        """ as String)
        }

        sql.executeUpdate("DROP DATABASE IF EXISTS $databaseName;".toString())
        sql.executeUpdate("DROP ROLE IF EXISTS $properties.dbuser;".toString())

        if (!dropOnly) {
            sql.executeUpdate("CREATE ROLE $properties.dbuser WITH LOGIN PASSWORD '$properties.dbpassword';".toString())
            sql.executeUpdate("CREATE DATABASE $databaseName OWNER $properties.dbuser;".toString())
            sql.executeUpdate("GRANT ALL PRIVILEGES ON DATABASE $databaseName TO $properties.dbuser;".toString())
        }
        sql.close()
    }

    private void checkRootCredentials(DatabasePluginExtension properties) {
        if (properties.dbRootUser == null || properties.dbRootUser.isEmpty() || properties.dbRootPassword == null || properties.dbRootPassword.isEmpty()) {
            throw new IllegalStateException("must specify db.root.user and db.root.password for ${properties.dbvendor}")
        }
    }

    private void cleanSqlServerDb(DatabasePluginExtension properties) {
        checkRootCredentials(properties)
        def (databaseName, genericUrl) = extractDataBaseNameAndGenericUrl(properties)

        Sql sql = newSqlInstance(genericUrl, properties.dbRootUser, properties.dbRootPassword, properties.dbdriverClass)

        executeSqlServerScript(sql, "/sqlserver-01-drop.sql", properties, databaseName)
        if (!dropOnly) {
            executeSqlServerScript(sql, "/sqlserver-02-create.sql", properties, databaseName)
        }

        sql.close()
    }

    private void executeSqlServerScript(Sql sql, String scriptPath, DatabasePluginExtension properties, String databaseName) {
        def script = this.getClass().getResourceAsStream(scriptPath).text
        script = script.replace("@sqlserver.db.name@", databaseName)
        script = script.replace("@sqlserver.connection.username@", properties.dbuser)
        script = script.replace("@sqlserver.connection.password@", properties.dbpassword)
        script.split("GO").each {
            logger.info "Executing query $it"
            sql.executeUpdate(it)
        }
    }

    private void cleanOracleDb(DatabasePluginExtension properties) {
        checkRootCredentials(properties)

        Properties props = [user: properties.dbRootUser, password: properties.dbRootPassword] as Properties
        Sql sql = newSqlInstance(properties.dburl, props, properties.dbdriverClass)

        def sqlQuery = """-- Drop/Create user script 
  declare
    v_count         number := 0;
    v_max_retries   number := 10;

  begin
  -- lock user if exists
  select count(1) into v_count from dba_users where upper(username) = upper('${properties.dbuser}');
  if v_count != 0
  then
    execute immediate 'ALTER USER ${properties.dbuser} ACCOUNT LOCK';
  end if;

  -- disconnect sessions
  for session_rec in (
            select s.sid, s.serial# from v\$session s
            where s.type != 'BACKGROUND' and ( upper(s.username) = upper('${properties.dbuser}') )
            )
  loop
    begin
      execute immediate 'ALTER SYSTEM DISCONNECT SESSION '''|| session_rec.sid || ',' || session_rec.serial# || ''' IMMEDIATE';
    EXCEPTION
      WHEN OTHERS THEN
        -- ORA-00030: User session ID does not exist. In this case, sesion has been dropped since we have performed the
        -- search query. So we can ignore this error.
        if SQLCODE != -30
        then
          RAISE;
        end if;
    end;
  end loop;

  -- for oracle 12c
  for cur_banner in ( select v.banner from v\$version v ) loop
    if cur_banner.banner LIKE 'Oracle Database 12c%'
    then
      execute immediate 'alter session set "_ORACLE_SCRIPT"=true';
    end if;
  end loop;

  -- drop user if exists
  FOR i IN 1 .. v_max_retries LOOP
    BEGIN
      select count(1) into v_count from dba_users where upper(username) = upper('${properties.dbuser}');
      if v_count != 0
      then
        execute immediate 'drop user ${properties.dbuser} cascade';
      end if;
      EXIT;
    EXCEPTION
      WHEN OTHERS THEN
      if i >= v_max_retries
      then
        RAISE;
      end if;
      dbms_lock.sleep( seconds => 5 ); -- Try to prevent ORA-01940: cannot drop a user that is currently connected
    END;
   END LOOP;
        """

        if (!dropOnly) {
            sqlQuery += """
  -- recreate user
  execute immediate 'CREATE USER ${properties.dbuser} IDENTIFIED BY ${properties.dbpassword}';
  execute immediate 'ALTER USER ${properties.dbuser} QUOTA 300M ON USERS';
  execute immediate 'GRANT connect, resource TO ${properties.dbuser}';
  execute immediate 'GRANT select ON sys.dba_pending_transactions TO ${properties.dbuser}';
  execute immediate 'GRANT select ON sys.pending_trans\$ TO ${properties.dbuser}';
  execute immediate 'GRANT select ON sys.dba_2pc_pending TO ${properties.dbuser}';
  execute immediate 'GRANT execute ON sys.dbms_system TO ${properties.dbuser}';
            """
        }
        sqlQuery += "end;"

        sql.execute(sqlQuery as String)
    }

}

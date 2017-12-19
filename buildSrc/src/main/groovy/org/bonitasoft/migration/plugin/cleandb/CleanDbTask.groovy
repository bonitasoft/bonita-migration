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

package org.bonitasoft.migration.plugin.cleandb

import groovy.sql.Sql
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * @author Baptiste Mesta
 */
class CleanDbTask extends DefaultTask {

    @Override
    String getDescription() {
        return "Drop and recreate the database in order to launch test in a clean one."
    }

    @TaskAction
    def cleanDb() {
        def CleanDbPluginExtension properties = project.database
        logger.info "Migration of ${properties.dbvendor}"
        List<URL> urls = new ArrayList<URL>()
        properties.classpath.each { File file ->
            urls.add(file.toURI().toURL())
        }
        //workaround classpath issue
        def loader1 = Sql.class.getClassLoader()
        properties.classpath.each { File file ->
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

    private List extractDataBaseNameAndGenericUrl(CleanDbPluginExtension properties) {
        def genericUrl, databaseName
        logger.info "drop and create: $properties.dburl"
        def parsedUrl = (properties.dburl =~ /(jdbc:\w+:\/\/)([\w\d\.-]+):(\d+)\/([\w\-_\d]+).*/)
        def serverName = parsedUrl[0][2]
        def portNumber = parsedUrl[0][3]
        databaseName = parsedUrl[0][4]
        genericUrl = parsedUrl[0][1] + serverName + ":" + portNumber + "/"
        logger.info "recreate database $databaseName on server $serverName and port $portNumber with driver $properties.dbdriverClass"
        logger.info "url is  $genericUrl"
        [databaseName, genericUrl]
    }

    private void cleanMysqlDb(CleanDbPluginExtension properties) {
        def (databaseName, genericUrl) = extractDataBaseNameAndGenericUrl(properties)
        checkRootCredentials(properties)

        def Sql sql = Sql.newInstance(genericUrl, properties.dbRootUser, properties.dbRootPassword, properties.dbdriverClass)
        sql.executeUpdate("DROP DATABASE IF EXISTS " + databaseName)
        sql.eachRow("SELECT DISTINCT user FROM mysql.user WHERE user ='" + properties.dbuser + "'") {
            sql.executeUpdate("DROP USER " + properties.dbuser)
        }
        sql.executeUpdate("CREATE USER " + properties.dbuser + " IDENTIFIED BY '" + properties.dbpassword + "'")
        sql.executeUpdate("CREATE DATABASE " + databaseName + " DEFAULT CHARACTER SET utf8")
        sql.executeUpdate("GRANT ALL ON " + databaseName + ".* TO " + properties.dbuser)
        sql.close()
    }

    private void cleanPostgresDb(CleanDbPluginExtension properties) {
        def (databaseName, genericUrl) = extractDataBaseNameAndGenericUrl(properties)
        checkRootCredentials(properties)
        def Sql sql = Sql.newInstance((String) genericUrl, (String) properties.dbRootUser, (String) properties.dbRootPassword, (String) properties.dbdriverClass)

        // postgres 9.3 script version
        sql.eachRow("""
                    SELECT pid
                    FROM pg_stat_activity
                    WHERE upper(pg_stat_activity.datname) = upper('$databaseName')
                      AND pid <> pg_backend_pid()
                    """ as String) {
            logger.info("disconnect connexion id $it.pid from database $databaseName")
            sql.execute("""
                    SELECT pg_terminate_backend(pg_stat_activity.pid)
                    FROM pg_stat_activity
                    WHERE pg_stat_activity.datname = '$databaseName'
                    AND pid = $it.pid
                        """ as String)
        }

        logger.info "cleaning postgres database $databaseName"
        sql.executeUpdate("DROP DATABASE IF EXISTS $databaseName;".toString())
        sql.executeUpdate("CREATE DATABASE $databaseName OWNER $properties.dbuser;".toString())
        sql.executeUpdate("GRANT ALL PRIVILEGES ON DATABASE $databaseName TO $properties.dbuser;".toString())
        sql.close()
    }

    private void checkRootCredentials(CleanDbPluginExtension properties) {
        if (properties.dbRootUser == null || properties.dbRootUser.isEmpty() || properties.dbRootPassword == null || properties.dbRootPassword.isEmpty()) {
            throw new IllegalStateException("must specify db.root.user and db.root.password for postgres")
        }
    }

    private void cleanSqlServerDb(CleanDbPluginExtension properties) {
        logger.info "cleaning sqlserver database $properties.dburl"
        if (properties.dbRootUser == null || properties.dbRootUser.isEmpty() || properties.dbRootPassword == null || properties.dbRootPassword.isEmpty()) {
            throw new IllegalStateException("must specify db.root.user and db.root.password for sqlserver")
        }

        def parsedUrl = (properties.dburl =~ /(jdbc:\w+:\/\/)([\w\d\.-]+):(\d+);database=([\w\-_\d]+).*/)
        def serverName = parsedUrl[0][2]
        def portNumber = parsedUrl[0][3]
        def databaseName = parsedUrl[0][4]
        def genericUrl = parsedUrl[0][1] + serverName + ":" + portNumber
        logger.info "recreate database $databaseName on server $serverName and port $portNumber with driver $properties.dbdriverClass"
        logger.info "url is  $genericUrl"
        def Sql sql = Sql.newInstance(genericUrl, properties.dbRootUser, properties.dbRootPassword, properties.dbdriverClass)
        def script = this.getClass().getResourceAsStream("/init-sqlserver.sql").text
        script = script.replace("@sqlserver.db.name@", databaseName)
        script = script.replace("@sqlserver.connection.username@", properties.dbuser)
        script = script.replace("@sqlserver.connection.password@", properties.dbpassword)
        script.split("GO").each { sql.executeUpdate(it) }
        sql.close()
    }

    private void cleanOracleDb(CleanDbPluginExtension properties) {
        logger.info "cleaning oracle database $properties.dbuser"
        if (properties.dbRootUser == null || properties.dbRootUser.isEmpty() || properties.dbRootPassword == null || properties.dbRootPassword.isEmpty()) {
            throw new IllegalStateException("must specify db.root.user and db.root.password for oracle")
        }
        def props = [user: properties.dbRootUser, password: properties.dbRootPassword] as Properties
        def Sql sql = Sql.newInstance(properties.dburl, props, properties.dbdriverClass)

        sql.execute("""
declare
  v_count number;
  v_banner varchar2(50) := 'Oracle Database 12c%';

  cursor c1 is
    select s.sid, s.serial#, s.username """
                + ''' from gv$session s ''' + """
    where s.type != 'BACKGROUND' and (upper(s.username) = upper('${properties.dbuser}') OR upper(s.username) = upper('${
            properties.dbuser
        }_bdm'));

  cursor c2 is
"""
                + ''' select v.banner from v$version v; '''
                + """
                    begin
  -- disconnect sessions
  for session_rec in c1
  loop
    execute immediate 'ALTER SYSTEM DISCONNECT SESSION session_rec.sid,session_rec.serial# IMMEDIATE';
  end loop;

  -- for oracle 12c
  for cur_banner in c2 loop
    if cur_banner.banner LIKE v_banner
    then
      execute immediate 'alter session set "_ORACLE_SCRIPT"=true';
    end if;
  end loop;


  -- drop user if exists
  select count(1) into v_count from dba_users where upper(username) = upper('${properties.dbuser}');
  if v_count != 0
  then
    execute immediate 'drop user ${properties.dbuser} cascade';
  end if;

  select count(1) into v_count from dba_users where upper(username) = upper('${properties.dbuser}_bdm');
  if v_count != 0
  then
    execute immediate 'drop user ${properties.dbuser}_bdm cascade';
  end if;

  -- recreate user
  execute immediate 'CREATE USER ${properties.dbuser} IDENTIFIED BY ${properties.dbpassword}';
  execute immediate 'ALTER USER ${properties.dbuser} QUOTA 300M ON USERS';
  execute immediate 'GRANT connect, resource TO ${properties.dbuser}';
  execute immediate 'GRANT select ON sys.dba_pending_transactions TO ${properties.dbuser}';
  execute immediate 'GRANT select ON sys.pending_trans\$ TO ${properties.dbuser}';
  execute immediate 'GRANT select ON sys.dba_2pc_pending TO ${properties.dbuser}';
  execute immediate 'GRANT execute ON sys.dbms_system TO ${properties.dbuser}';

  execute immediate 'CREATE USER ${properties.dbuser}_bdm IDENTIFIED BY ${properties.dbpassword}';
  execute immediate 'ALTER USER ${properties.dbuser}_bdm QUOTA 300M ON USERS';
  execute immediate 'GRANT connect, resource TO ${properties.dbuser}_bdm';
  execute immediate 'GRANT select ON sys.dba_pending_transactions TO ${properties.dbuser}_bdm';
  execute immediate 'GRANT select ON sys.pending_trans\$ TO ${properties.dbuser}_bdm';
  execute immediate 'GRANT select ON sys.dba_2pc_pending TO ${properties.dbuser}_bdm';
  execute immediate 'GRANT execute ON sys.dbms_system TO ${properties.dbuser}_bdm';
end;
"""
        )
    }
}

package org.bonitasoft.migration.core

import groovy.sql.Sql

import java.sql.ResultSet
import groovy.util.AntBuilder;


public class MigrationUtil {

    public static String SOURCE_VERSION = "source.version"

    public static String TARGET_VERSION = "target.version"

    public static String DB_URL = "db.url"

    public static String DB_USER = "db.user"

    public static String DB_PASSWORD = "db.password"

    public static String DB_DRIVERCLASS = "db.driverclass"

    public static String DB_VENDOR = "db.vendor"

    public static String BONITA_HOME = "bonita.home"

    public static String REQUEST_SEPARATOR = "@@"


    public static Map parseOrAskArgs(String[] args){
        //will ask for missing parameter
        return listToMap(args)
    }

    public static Map listToMap(String[] list){
        def map = [:]
        def iterator = list.iterator()
        while (iterator.hasNext()) {
            map.put(iterator.next().substring(2),iterator.next())
        }
        return map
    }

    public static Sql getSqlConnection(Map props){
        def dburl = props.get(MigrationUtil.DB_URL)
        def user = props.get(MigrationUtil.DB_USER)
        def pass = props.get(MigrationUtil.DB_PASSWORD)
        def driver = props.get(MigrationUtil.DB_DRIVERCLASS)
        println "url=" + dburl
        println "user=" + user
        println "pass=" + pass
        println "driver=" + driver
        return Sql.newInstance(dburl, user, pass, driver)
    }

    public static executeDefaultSqlFile(File file, String dbVendor, groovy.sql.Sql sql){
        executeSqlFile(file, dbVendor, null, null, sql, true)
    }

    public static executeSqlFile(File feature, String dbVendor, String suffix, Map<String, String> parameters, groovy.sql.Sql sql, boolean toUpdate){
        def sqlFile = getSqlFile(feature, dbVendor, suffix)
        sql.withTransaction {
            if(sqlFile.exists()){
                def contents = getSqlContent(sqlFile.text, parameters)

                for (content in contents) {
                    if (!content.trim().empty) {
                        if (toUpdate) {
                            println sql.executeUpdate(content) + " row(s) updated"
                        } else {
                            sql.execute(content)
                        }
                    }
                }
            } else{
                println "nothing to execute"
            }
        }
    }

    public static String[] getSqlContent(String sqlFileContent, Map<String, String> parameters){
        def sqlFileContentWithParameters = replaceParameters(sqlFileContent, parameters).replaceAll("\r\n", "\n")
        return sqlFileContentWithParameters.split(REQUEST_SEPARATOR)
    }

    public static File getSqlFile(File folder, String dbVendor, String suffix){
        return new File(folder, dbVendor + (suffix == null || suffix.isEmpty() ? "" : "-" + suffix) + ".sql")
    }

    static String replaceParameters(String sqlFileContent, Map<String, String> parameters){
        String newSqlFileContent = sqlFileContent
        if (parameters != null) {
            for (parameter in parameters) {
                newSqlFileContent = newSqlFileContent.replaceAll(parameter.key, String.valueOf(parameter.value))
            }
        }
        return newSqlFileContent
    }

    public static Object getId(File feature, String dbVendor, String fileExtension, Object it, groovy.sql.Sql sql){
        def sqlFile = getSqlFile(feature, dbVendor, fileExtension)
        def parameters = Collections.singletonMap(":tenantId", String.valueOf(it))
        def id = null
        sql.eachRow(getSqlContent(sqlFile.text, parameters)[0]) { row ->
            id = row[0]
        }
        return id
    }

    public static List<Object> getTenantsId(File feature, String dbVendor, groovy.sql.Sql sql){
        def sqlFile = getSqlFile(feature, dbVendor, "tenants")
        def tenants = []

        sql.query(getSqlContent(sqlFile.text, null)[0]) { ResultSet rs ->
            while (rs.next()) tenants.add(rs.getLong(1))
        }
        return tenants
    }

    public static migrateDirectory(String fromDir, String toDir){
        def ant = new AntBuilder();
        def deleted = false
        if (!(deleted = new File(toDir).deleteDir())) {
            throw IllegalStateException("Unable to delete: " + toDir)
        } else {
            ant.copy(todir: toDir) { fileset(dir: fromDir) }
            println toDir + " succesfully migrated"
        }
    }
}

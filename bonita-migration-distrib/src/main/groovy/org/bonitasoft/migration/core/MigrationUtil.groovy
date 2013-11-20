package org.bonitasoft.migration.core

import groovy.sql.Sql

import java.sql.ResultSet
import java.util.Properties;
import groovy.util.AntBuilder;
import org.bonitasoft.migration.core.exception.NotFoundException;
import org.bonitasoft.migration.core.exception.MigrationException;



public class MigrationUtil {

    public static String SOURCE_VERSION = "source.version"

    public static String TARGET_VERSION = "target.version"

    public static String BONITA_HOME = "bonita.home"

    public static String DB_URL = "db.url"

    public static String DB_USER = "db.user"

    public static String DB_PASSWORD = "db.password"

    public static String DB_DRIVERCLASS = "db.driverClass"

    public static String DB_VENDOR = "db.vendor"

    public static String REQUEST_SEPARATOR = "@@"

    public static Properties getProperties(){
        def Properties properties = new Properties();
        def FileInputStream fileInputStream = null;

        try {
            // Load a properties file
            fileInputStream = new FileInputStream("Config.properties");
            properties.load(fileInputStream);
        } catch (java.io.FileNotFoundException e) {
            throw new NotFoundException("File Config.properties not found : " + e);
        } catch (IOException e) {
            throw new MigrationException("Can't get all properties to migrate : " + e);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return properties;
    }

    public static String displayProperty(Properties properties, String propertyName) {
        if (properties == null || propertyName == null || "".equals(propertyName)){
            throw new IllegalArgumentException("Can't execute displayProperty method with arguments : propeties = " + properties + ", propertyName = " + propertyName);
        }
        
        def String property = properties.getProperty(propertyName);
        if (property != null) {
            println propertyName + " = " + property
        } else {
            throw new NotFoundException("The property " + propertyName + " doesn't exist !!");
        }
        return property;
    }

    public static Sql getSqlConnection(String dburl, String user, String pwd, String driverClass){
        return Sql.newInstance(dburl, user, pwd, driverClass);
    }

    public static executeDefaultSqlFile(File file, String dbVendor, groovy.sql.Sql sql){
        executeSqlFile(file, dbVendor, null, null, sql, true)
    }

    public static executeSqlFile(File feature, String dbVendor, String suffix, Map<String, String> parameters, groovy.sql.Sql sql, boolean toUpdate){
        def sqlFile = getSqlFile(feature, dbVendor, suffix)
        sql.withTransaction {
            if(sqlFile.exists()){
                def List<String> contents = getSqlContent(sqlFile.text, parameters)

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

    public static List<String> getSqlContent(String sqlFileContent, Map<String, String> parameters){
        def sqlFileContentWithParameters = replaceParameters(sqlFileContent, parameters).replaceAll("\r", "")
        return Arrays.asList(sqlFileContentWithParameters.split(REQUEST_SEPARATOR))
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
        sql.eachRow(getSqlContent(sqlFile.text, parameters).get(0)) {
            row ->
            id = row[0]
        }
        return id
    }

    public static List<Object> getTenantsId(File feature, String dbVendor, groovy.sql.Sql sql){
        def sqlFile = getSqlFile(feature, dbVendor, "tenants")
        def tenants = []

        sql.query(getSqlContent(sqlFile.text, null).get(0)) {
            ResultSet rs ->
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

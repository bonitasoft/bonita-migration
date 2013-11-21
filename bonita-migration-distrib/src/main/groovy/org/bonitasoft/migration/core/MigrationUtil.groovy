package org.bonitasoft.migration.core

import groovy.sql.Sql

import java.sql.ResultSet

import org.bonitasoft.migration.core.exception.MigrationException
import org.bonitasoft.migration.core.exception.NotFoundException



public class MigrationUtil {

    public final static String FILE_SEPARATOR = System.getProperty("file.separator");

    public final static String SOURCE_VERSION = "source.version"

    public final static String TARGET_VERSION = "target.version"

    public final static String BONITA_HOME = "bonita.home"

    public final static String DB_URL = "db.url"

    public final static String DB_USER = "db.user"

    public final static String DB_PASSWORD = "db.password"

    public final static String DB_DRIVERCLASS = "db.driverClass"

    public final static String DB_VENDOR = "db.vendor"

    public final static String REQUEST_SEPARATOR = "@@"

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

    public static String getAndDisplayProperty(Properties properties, String propertyName) {
        if (properties == null || propertyName == null || "".equals(propertyName)){
            throw new IllegalArgumentException("Can't execute getAndDisplayProperty method with arguments : propeties = " + properties + ", propertyName = " + propertyName);
        }

        def String property = properties.getProperty(propertyName);
        if (property != null) {
            property = property.replaceAll("\t", "").replaceAll(" ", "");
            println "\t-" + propertyName + " = " + property
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
        if (folder == null || dbVendor == null || "".equals(dbVendor)){
            throw new IllegalArgumentException("Can't execute getSqlFile method with arguments : folder = " + folder + ", dbVendor = " + dbVendor);
        }
        return new File(folder, dbVendor + (suffix == null || suffix.isEmpty() ? "" : "-" + suffix) + ".sql")
    }

    static String replaceParameters(String sqlFileContent, Map<String, String> parameters){
        if (sqlFileContent == null){
            throw new IllegalArgumentException("Can't execute replaceParameters method with arguments : sqlFileContent = " + sqlFileContent);
        }

        String newSqlFileContent = sqlFileContent
        if (parameters != null) {
            for (parameter in parameters) {
                newSqlFileContent = newSqlFileContent.replaceAll(parameter.key, String.valueOf(parameter.value))
            }
        }
        return newSqlFileContent
    }

    public static Object getId(File feature, String dbVendor, String fileExtension, Object it, groovy.sql.Sql sql){
        if (it == null || sql == null){
            throw new IllegalArgumentException("Can't execute getId method with arguments : it = " + it + ", sql = " + sql);
        }

        def sqlFile = getSqlFile(feature, dbVendor, fileExtension)
        def parameters = Collections.singletonMap(":tenantId", String.valueOf(it))
        def id = null
        sql.eachRow(getSqlContent(sqlFile.text, parameters).get(0)) { row ->
            id = row[0]
        }
        return id
    }

    public static List<Object> getTenantsId(File feature, String dbVendor, groovy.sql.Sql sql){
        if (sql == null){
            throw new IllegalArgumentException("Can't execute getTenantsId method with arguments : sql = " + sql);
        }

        def sqlFile = getSqlFile(feature, dbVendor, "tenants")
        def tenants = []

        sql.query(getSqlContent(sqlFile.text, null).get(0)) { ResultSet rs ->
            while (rs.next()) tenants.add(rs.getLong(1))
        }
        return tenants
    }

    public static migrateDirectory(String fromDir, String toDir){
        if (fromDir == null || toDir == null){
            throw new IllegalArgumentException("Can't execute migrateDirectory method with arguments : fromDir = " + fromDir + ", toDir = " + toDir);
        }

        def ant = new AntBuilder();
        def deleted = false
        if (!(deleted = new File(toDir).deleteDir())) {
            throw IllegalStateException("Unable to delete : " + toDir)
        } else {
            ant.copy(todir: toDir) { fileset(dir: fromDir) }
            println toDir + " succesfully migrated"
        }
    }

    public static Object deserialize(byte[] bytes, ClassLoader theClassLoader){
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes)){
                    protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                        return Class.forName(objectStreamClass.getName(), true, theClassLoader);
                    }
                };
        try {
            return input.readObject();
        } finally {
            input.close();
        }
    }

    public static byte[] serialize(Object object){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        out = new ObjectOutputStream(baos);
        out.writeObject(object);
        out.flush();
        return baos.toByteArray();
    }
}

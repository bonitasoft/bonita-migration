package org.bonitasoft.migration.core

import groovy.sql.Sql
import groovy.time.TimeCategory

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

    public final static String AUTO_ACCEPT = "auto.accept"


    public final static String REQUEST_SEPARATOR = "@@"


    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

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

    public static String getAndPrintProperty(Properties properties, String propertyName) {
        if (properties == null || propertyName == null || "".equals(propertyName)){
            throw new IllegalArgumentException("Can't execute getAndPrintProperty method with arguments : propeties = " + properties + ", propertyName = " + propertyName);
        }

        def String property = System.getProperty(propertyName);
        if(property == null){
            property = properties.getProperty(propertyName);
        }
        if (property != null) {
            property = property.replaceAll("\t", "").trim();
            println "\t-" + propertyName + " = " + property
        } else {
            throw new NotFoundException("The property " + propertyName + " doesn't exist !!");
        }
        return property;
    }

    /**
     * @param nbTabs
     *      Number of tabulations to display
     * @return Old System.out
     * @since 6.1
     */
    public static PrintStream setSystemOutWithTab(int nbTabs){
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(stdout){
                    @Override
                    public void println(String x) {
                        if (nbTabs != 0){
                            for (int i = 0; i < nbTabs; i++){
                                super.print("   |");
                            }
                            super.print(" ");
                        }
                        super.println(x);
                    }
                });
        return stdout;
    }

    public static executeMigration(GroovyScriptEngine gse, File file, String scriptName, Binding binding, int nbTabs, Date startMigrationDate){
        def startFeatureDate = new Date();
        PrintStream stdout = setSystemOutWithTab(nbTabs);
        gse.run(new File(file, scriptName).getPath(), binding)
        System.setOut(stdout);
        printSuccessMigration(startFeatureDate, startMigrationDate);
    }

    public static printSuccessMigration(Date startFeatureDate, Date startMigrationDate){
        if (startFeatureDate == null || startMigrationDate == null){
            throw new IllegalArgumentException("Can't execute printSuccessMigration method with arguments : startFeatureDate = " + startFeatureDate + ", startMigrationDate = " + startMigrationDate);
        }

        def endFeatureDate = new Date()
        println "[ Success in "+ TimeCategory.minus(endFeatureDate, startFeatureDate) + ". The migration started, there is " + TimeCategory.minus(endFeatureDate, startMigrationDate) + " ]"
        println ""
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

    public static getAndDisplayPlatformVersion(groovy.sql.Sql sql){
        sql.eachRow("SELECT version FROM platform") { row ->
            println "The platform version in database is : " + row[0] + "."
        }
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

    public static migrateDirectory(String fromDir, String toDir, boolean deleteOldDirectory){
        if (fromDir == null || toDir == null){
            throw new IllegalArgumentException("Can't execute migrateDirectory method with arguments : fromDir = " + fromDir + ", toDir = " + toDir);
        }

        println "Migration of " + toDir + "..."
        def fileFromDir = new File(fromDir);
        def fileToDir = new File(toDir);
        if (!fileFromDir.exists() || !fileFromDir.isDirectory()) {
            throw new IllegalStateException("Migration failed. Source folder does not exist : " + fromDir)
        }
        if (deleteOldDirectory && !(fileToDir.deleteDir())) {
            throw new IllegalStateException("Migration failed. Unable to delete : " + toDir)
        }
        copyDirectory(fileFromDir, fileToDir)
        println "Done"
    }

    private static void copyDirectory(File srcDir, File destDir) throws IOException {
        if (!destDir.exists()){
            if (destDir.mkdirs() == false) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            destDir.setLastModified(srcDir.lastModified());
            if (destDir.canWrite() == false) {
                throw new IOException("Destination '" + destDir + "' cannot be written to");
            }
        }
        // recurse
        File[] files = srcDir.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }
        for (int i = 0; i < files.length; i++) {
            File copiedFile = new File(destDir, files[i].getName());
            if (files[i].isDirectory()) {
                copyDirectory(files[i], copiedFile);
            } else {
                copyFile(files[i], copiedFile);
            }
        }
    }
    private static void copyFile(File srcFile, File destFile) throws IOException {
        if (destFile.exists() && !(destFile.delete())) {
            throw new IllegalStateException("Migration failed. Unable to delete : " + destFile)
        }

        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                long count = 0;
                int n = 0;
                while (-1 != (n = input.read(buffer))) {
                    output.write(buffer, 0, n);
                    count += n;
                }
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
            srcFile + "' to '" + destFile + "'");
        }
        destFile.setLastModified(srcFile.lastModified());
    }

    public static Object deserialize(byte[] bytes, ClassLoader theClassLoader){
        //had to override the method of objectinputstream to be able to give the object classloader in input
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

    public static boolean isAutoAccept(){
        return System.getProperty(MigrationUtil.AUTO_ACCEPT)=="true"
    }
}

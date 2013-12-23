/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.migration.core

import groovy.sql.Sql
import groovy.time.TimeCategory

import java.sql.ResultSet

import org.bonitasoft.migration.core.exception.MigrationException
import org.bonitasoft.migration.core.exception.NotFoundException


/**
 *
 * Util classes that contains common methods for migration
 *
 * @author Baptiste Mesta
 *
 */
public class MigrationUtil {

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

    public final static String LINE_SEPARATOR = System.getProperty("line.separator");


    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;


    public static read = System.in.newReader().&readLine

    /**
     * Load properties form the 'Config.properties' file inside the distribution
     * @return the properties of the migration distribution
     */
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
    /**
     * Get a single property and print it
     * First it try to get it from system (in order to override properties)
     * then from the given property object
     */
    public static String getAndPrintProperty(Properties properties, String propertyName, boolean isMandatory) {
        if (properties == null || propertyName == null || "".equals(propertyName)){
            throw new IllegalArgumentException("Can't execute getAndPrintProperty method with arguments : propeties = " + properties + ", propertyName = " + propertyName);
        }
        def String property = System.getProperty(propertyName);
        if(property != null){
            println "" + propertyName + " = " + property
            return property
        }
        property = properties.getProperty(propertyName);
        if (property != null) {
            property = property.trim()
            println "" + propertyName + " = " + property
            return property
        }
        if(isMandatory){
            throw new NotFoundException("The property " + propertyName + " doesn't exist !!");
        }
    }


    /**
     *
     *  Wrap the system out with ' | ' when executing the closure
     */
    public static void executeWrappedWithTabs(Closure closure){
        PrintStream stdout = System.out;

        System.setOut(new PrintStream(stdout){
                    @Override
                    public void println(String x) {
                        stdout.print(" | ")
                        stdout.println(x)
                    }
                })
        closure.call()
        System.setOut(stdout);
    }

    /**
     * Execute a feature migration script
     *
     * @param gse
     * @param file
     * @param scriptName
     * @param binding
     * @param nbTabs
     * @param startMigrationDate
     * @return
     */
    public static executeMigration(GroovyScriptEngine gse, File file, String scriptName, Binding binding, Date startMigrationDate){
        def startDate = new Date();
        executeWrappedWithTabs {
            gse.run(new File(file, scriptName).getPath(), binding)
        }
        MigrationUtil.printSuccessMigration(startDate, startMigrationDate);
    }

    public static printSuccessMigration(Date startFeatureDate, Date startMigrationDate){
        if (startFeatureDate == null || startMigrationDate == null){
            throw new IllegalArgumentException("Can't execute printSuccessMigration method with arguments : startFeatureDate = " + startFeatureDate + ", startMigrationDate = " + startMigrationDate);
        }

        def endFeatureDate = new Date()
        println "[ Migration step success in "+ TimeCategory.minus(endFeatureDate, startFeatureDate) + ". Migration started  " + TimeCategory.minus(endFeatureDate, startMigrationDate) + " ago. ]"
        println ""
    }

    public static Sql getSqlConnection(String dburl, String user, String pwd, String driverClass){
        return Sql.newInstance(dburl, user, pwd, driverClass);
    }

    public static executeDefaultSqlFile(File file, String dbVendor, groovy.sql.Sql sql){
        executeSqlFile(file, dbVendor, null, null, sql, true)
    }

    /**
     * execute a sql file
     * @param feature
     *      the folder where the feature to migrate is
     * @param dbVendor
     *      the current database type
     * @param suffix
     *      the suffix of the file to execute, format is: <dbVendor>-<suffixe>.sql
     * @param parameters
     *      parameters to replace inside the file, if parameter 'tenant' is given, all word 'tenant' will be replace by the value
     * @param sql
     *      the sql connection to the database
     * @param toUpdate
     *      if this will update elements
     */
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

    public static String getPlatformVersion(groovy.sql.Sql sql){
        def version = null;
        sql.eachRow("SELECT version FROM platform") { row ->
            version = row[0]
        }
        return version;
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
        if(deleteOldDirectory){
            println "Replacing all content of $toDir..."
        }else{
            println "Adding/overwriting content in $toDir..."
        }
        def fileFromDir = new File(fromDir);
        def fileToDir = new File(toDir);
        if (!fileFromDir.exists() || !fileFromDir.isDirectory()) {
            throw new IllegalStateException("Migration failed. Source folder does not exist : " + fromDir)
        }
        if (deleteOldDirectory && !(fileToDir.deleteDir())) {
            throw new IllegalStateException("Migration failed. Unable to delete : " + toDir)
        }
        copyDirectory(fileFromDir, fileToDir)
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
    static void printInRectangle(String... lines){
        def maxSize = lines.collect{ it.size() }.max() +2
        printLine(maxSize)
        lines.each {
            int spaces = maxSize - it.size()
            print "|"
            printSpaces((int)(spaces/2))
            print it
            printSpaces(((int)(spaces/2)) + spaces%2)
            print "|"
            print LINE_SEPARATOR
        }
        printLine(maxSize)

    }

    static printSpaces(int size){
        int i = 0;
        while (i<size) {
            i++;
            print ' '
        }
    }
    static printLine(int size){
        print '+'
        int i = 0;
        while (i<size) {
            i++;
            print '-'
        }
        print '+'
        print LINE_SEPARATOR
    }

    public static void  askIfWeContinue(){
        if(!MigrationUtil.isAutoAccept()){
            print "Continue migration? (yes/no): "
            String input = read()
            if(input != "yes"){
                println "Migration cancelled"
                System.exit(0);
            }
        }
    }
    public static String askForOptions(List<String> options){
        def input = null;
        while(true){
            options.eachWithIndex {it,idx->
                println "${idx+1} -- $it "
            }
            print "choice: "
            input = MigrationUtil.read();
            try{
                def choiceNumber = Integer.valueOf(input) -1 //index in the list is -1
                if(choiceNumber <= options.size()){
                    return options.get(choiceNumber)
                }
            }catch (Exception e){
            }
            println "Invalid choice, please enter a value between 1 and ${options.size()}"
        }
    }
}


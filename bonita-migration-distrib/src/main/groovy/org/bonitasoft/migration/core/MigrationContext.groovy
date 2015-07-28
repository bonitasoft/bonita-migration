package org.bonitasoft.migration.core

import groovy.sql.Sql
import org.bonitasoft.migration.core.exception.MigrationException
import org.bonitasoft.migration.core.exception.NotFoundException

/**
 * @author Baptiste Mesta
 */
class MigrationContext {

    public final static String SOURCE_VERSION = "source.version"
    public final static String TARGET_VERSION = "target.version"
    public final static String BONITA_HOME = "bonita.home"
    public final static String DB_URL = "db.url"
    public final static String DB_USER = "db.user"
    public final static String DB_PASSWORD = "db.password"
    public final static String DB_DRIVERCLASS = "db.driverClass"
    public final static String DB_VENDOR = "db.vendor"

    def MigrationStep.DBVendor dbVendor
    def Sql sql
    def File bonitaHome
    def String dburl
    def private Properties properties
    String dbDriverClassName
    String dbUser
    String dbPassword
    String sourceVersion
    String targetVersion


    public MigrationContext() {
        loadProperties()
    }

    /**
     * Load properties form the 'Config.properties' file inside the distribution
     * @return the properties of the migration distribution
     */
    public void loadProperties() {
        properties = new Properties();
        def fileInputStream = null;

        try {
            fileInputStream = new FileInputStream("Config.properties");
            println "using file " + (new File("Config.properties")).absolutePath
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new NotFoundException("File Config.properties not found : " + e);
        } catch (IOException e) {
            throw new MigrationException("Can't get all properties to migrate : " + e);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        dbVendor = MigrationStep.DBVendor.valueOf(properties.getProperty(DB_VENDOR).toUpperCase())
        dburl = properties.getProperty(DB_URL)
        dbDriverClassName = properties.getProperty(DB_DRIVERCLASS)
        dbUser = properties.getProperty(DB_USER)
        dbPassword = properties.getProperty(DB_PASSWORD)
        sourceVersion = properties.getProperty(SOURCE_VERSION)
        targetVersion = properties.getProperty(TARGET_VERSION)
        bonitaHome = new File(properties.getProperty(BONITA_HOME))
    }

    def openSqlConnection() {
        sql = MigrationUtil.getSqlConnection(dburl, dbUser, dbPassword, dbDriverClassName)
    }
    def claseSqlConnection() {
        sql.close()
        sql = null
    }
}

package org.bonitasoft.migration.core

import groovy.sql.Sql

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
        def configFile = new File("Config.properties")
        try {
            new FileInputStream(configFile).withStream {
                println "using file " + configFile.absolutePath
                properties.load(it);
            }
        } catch (IOException ignored) {
            println "failed to load $configFile.absolutePath"
        }
        dbVendor = MigrationStep.DBVendor.valueOf(getSystemPropertyOrFromConfigFile(DB_VENDOR, properties).toUpperCase())
        dburl = getSystemPropertyOrFromConfigFile(DB_URL, properties)
        dbDriverClassName = getSystemPropertyOrFromConfigFile(DB_DRIVERCLASS, properties)
        dbUser = getSystemPropertyOrFromConfigFile(DB_USER, properties)
        dbPassword = getSystemPropertyOrFromConfigFile(DB_PASSWORD, properties)
        sourceVersion = getSystemPropertyOrFromConfigFile(SOURCE_VERSION, properties)
        targetVersion = getSystemPropertyOrFromConfigFile(TARGET_VERSION, properties)
        bonitaHome = new File(getSystemPropertyOrFromConfigFile(BONITA_HOME, properties))
    }

    private static String getSystemPropertyOrFromConfigFile(String property, Properties properties) {
        def systemProp = System.getProperty(property)
        def propertyFromFile = properties.getProperty(property)
        if (systemProp != null) {
            println "Using property $property overrided by system property (instead of $propertyFromFile): $systemProp"
            return systemProp
        }
        if (propertyFromFile != null) {
            println "Using property $property from configuration file: $propertyFromFile"
            return propertyFromFile
        }
        throw new IllegalStateException("The property $property is neither set in system property nor in the configuration file ")
    }

    def openSqlConnection() {
        sql = MigrationUtil.getSqlConnection(dburl, dbUser, dbPassword, dbDriverClassName)
    }

    def closeSqlConnection() {
        sql.close()
        sql = null
    }
}

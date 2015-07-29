package org.bonitasoft.migration.filler

/**
 * @author Baptiste Mesta
 */
class FillerUtils {

    public static void initializeEngineSystemProperties() {
        System.setProperty("sysprop.bonita.db.vendor", System.getProperty("dbvendor"));
        System.setProperty("db.url", System.getProperty("dburl"));
        System.setProperty("db.user", System.getProperty("dbuser"));
        System.setProperty("db.password", System.getProperty("dbpassword"));
        def split = System.getProperty("dburl").split("/")
        def databaseName = split[split.length - 1]
        System.setProperty("db.database.name", databaseName);
        println "Database name parsed from the url: " + databaseName
    }
}

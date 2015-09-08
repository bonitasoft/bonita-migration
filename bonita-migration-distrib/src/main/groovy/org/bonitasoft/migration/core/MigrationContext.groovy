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

package org.bonitasoft.migration.core

import groovy.sql.Sql
import org.bonitasoft.migration.core.database.DatabaseHelper

/**
 * @author Baptiste Mesta
 */
class MigrationContext {

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
    String dbDriverClassName
    String dbUser
    String dbPassword
    String sourceVersion
    String targetVersion
    Logger logger
    DatabaseHelper databaseHelper


    public MigrationContext() {
    }

    /**
     * Load properties form the 'Config.properties' file inside the distribution
     * @return the properties of the migration distribution
     */
    public void loadProperties() {
        Properties properties = new Properties();
        def configFile = new File("../Config.properties")
        try {
            new FileInputStream(configFile).withStream {
                logger.info "Using file " + configFile.absolutePath
                properties.load(it);
            }
        } catch (IOException ignored) {
            logger.warn "Failed to load $configFile.absolutePath"
        }
        dbVendor = MigrationStep.DBVendor.valueOf(getSystemPropertyOrFromConfigFile(DB_VENDOR, properties, true).toUpperCase())
        dburl = getSystemPropertyOrFromConfigFile(DB_URL, properties, true)
        dbDriverClassName = getSystemPropertyOrFromConfigFile(DB_DRIVERCLASS, properties, true)
        dbUser = getSystemPropertyOrFromConfigFile(DB_USER, properties, true)
        dbPassword = getSystemPropertyOrFromConfigFile(DB_PASSWORD, properties, true)
        //if not set it will be ask later
        targetVersion = getSystemPropertyOrFromConfigFile(TARGET_VERSION, properties, false)
        bonitaHome = new File(getSystemPropertyOrFromConfigFile(BONITA_HOME, properties, true))
    }

    private String getSystemPropertyOrFromConfigFile(String property, Properties properties, boolean mandatory) {
        def systemProp = System.getProperty(property)
        def propertyFromFile = properties.getProperty(property)
        if (systemProp != null) {
            logger.info "Using property $property overrided by system property (instead of $propertyFromFile): $systemProp"
            return systemProp
        }
        if (propertyFromFile != null) {
            logger.info "Using property $property from configuration file: $propertyFromFile"
            return propertyFromFile
        }
        if (mandatory) {
            throw new IllegalStateException("The property $property is neither set in system property nor in the configuration file ")
        }
        return null;
    }

    def openSqlConnection() {
        sql = MigrationUtil.getSqlConnection(dburl, dbUser, dbPassword, dbDriverClassName)
        databaseHelper = new DatabaseHelper(dbVendor: dbVendor, sql: sql)
    }

    def closeSqlConnection() {
        sql.close()
        sql = null
    }

    def setVersion(String version){
        databaseHelper.setVersion(version)
    }
}

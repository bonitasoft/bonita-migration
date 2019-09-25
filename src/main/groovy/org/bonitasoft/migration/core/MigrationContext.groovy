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

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.ORACLE
import static org.bonitasoft.migration.core.database.DbConfig.*

import javax.sql.DataSource
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import org.apache.commons.dbcp2.BasicDataSource
import org.bonitasoft.migration.core.database.ConfigurationHelper
import org.bonitasoft.migration.core.database.DatabaseHelper
import org.bonitasoft.migration.core.database.DbConfig
import com.github.zafarkhaja.semver.Version

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
class MigrationContext {
    static {
        String.metaClass.toVersion = {
            Version.valueOf(delegate)
        }
    }

    public final static String TARGET_VERSION = "target.version"

    public final static String BONITA_HOME = "bonita.home"
    public final static String LOGGER_LEVEL = "logger.level"
    MigrationStep.DBVendor dbVendor

    ThreadLocal<Sql> sql
    File bonitaHome
    Version sourceVersion
    Version targetVersion
    Logger logger
    DatabaseHelper databaseHelper
    ConfigurationHelper configurationHelper

    private LoggingConfiguration loggingConfiguration = new LoggingConfiguration()

    private DataSource dataSource
    DbConfig dbConfig
    private ExecutorService executorService
    boolean verifyOnly

    MigrationContext() {
    }

    void start() {
        loggingConfiguration.initializeConfiguration()
    }

    /**
     * Load properties form the 'Config.properties' file inside the distribution
     * @return the properties of the migration distribution
     */
    void loadConfiguration() {
        logger.info('Loading configuration')
        Properties properties = new Properties()
        def configFile = new File("../Config.properties")
        try {
            new FileInputStream(configFile).withStream {
                logger.info "Using file " + configFile.absolutePath
                properties.load(it)
            }
        } catch (IOException ignored) {
            logger.warn "Failed to load $configFile.absolutePath"
        }
        dbVendor = MigrationStep.DBVendor.valueOf(getSystemPropertyOrFromConfigFile(DB_VENDOR, properties, true).toUpperCase())

        dbConfig = new DbConfig()
        dbConfig.dburl = getSystemPropertyOrFromConfigFile(DB_URL, properties, true)
        dbConfig.dbDriverClassName = getSystemPropertyOrFromConfigFile(DB_DRIVER_CLASS, properties, true)
        dbConfig.dbUser = getSystemPropertyOrFromConfigFile(DB_USER, properties, true)
        dbConfig.dbPassword = getSystemPropertyOrFromConfigFile(DB_PASSWORD, properties, true)

        def dbPoolSizeInitial = getSystemPropertyOrFromConfigFileAsInteger(DB_POOL_SIZE_INITIAL, properties, false)
        if (dbPoolSizeInitial > -1) {
            dbConfig.dbPoolSizeInitial = dbPoolSizeInitial
        }
        def dbPoolSizeMax = getSystemPropertyOrFromConfigFileAsInteger(DB_POOL_SIZE_MAX, properties, false)
        if (dbPoolSizeMax > -1) {
            dbConfig.dbPoolSizeMax = dbPoolSizeMax
        }

        //if not set it will be ask later
        targetVersion = getSystemPropertyOrFromConfigFile(TARGET_VERSION, properties, false)?.toVersion()

        def file = getSystemPropertyOrFromConfigFile(BONITA_HOME, properties, false)
        if (file != null) {
            bonitaHome = new File(file)
        }
        completeLoggingConfiguration(properties)

        logger.info('Configuration loading completed')
    }

    private void completeLoggingConfiguration(Properties properties) {
        def level = getSystemPropertyOrFromConfigFile(LOGGER_LEVEL, properties, false)
        loggingConfiguration.configureMigrationLogger(level)
    }

    private String getSystemPropertyOrFromConfigFile(String property, Properties properties, boolean mandatory) {
        def systemProp = System.getProperty(property)
        def propertyFromFile = properties.getProperty(property)
        if (systemProp != null) {
            logger.info "Using property $property overriden by system property (instead of ${hidePasswordValue(property, propertyFromFile)}): ${hidePasswordValue(property, systemProp)}"
            return systemProp
        }
        if (propertyFromFile != null) {
            logger.info "Using property $property from configuration file: ${hidePasswordValue(property, propertyFromFile)}"
            return propertyFromFile
        }
        if (mandatory) {
            throw new IllegalStateException("The property $property is neither set in system property nor in the configuration file ")
        }
        return null
    }

    private int getSystemPropertyOrFromConfigFileAsInteger(String property, Properties properties, boolean mandatory) {
        String string = getSystemPropertyOrFromConfigFile(property, properties, mandatory)
        if (string != null) {
            try {
                return Integer.valueOf(string)
            }
            catch (RuntimeException e) {
                logger.warn("Unable to convert the $property property value $string into Integer, so use default")
            }
        }
        -1
    }

    private String hidePasswordValue(String property, String value) {
        isPasswordProperty(property) ? "*****" : value
    }

    private boolean isPasswordProperty(String property) {
        property.contains("password")
    }

    def openSqlConnection() {
        executorService = Executors.newFixedThreadPool(dbConfig.dbPoolSizeMax)
        setupDataSource()
        sql = ThreadLocal.<Sql> withInitial({ new Sql(dataSource) })

        databaseHelper = new DatabaseHelper(dbVendor: dbVendor, sql: getSql(), logger: logger)
        configurationHelper = new ConfigurationHelper(sql: getSql(), logger: logger, databaseHelper: databaseHelper)
    }

    private setupDataSource() {
        def dataSource = new BasicDataSource()
        dataSource.defaultAutoCommit = true
        dataSource.setDriverClassName(dbConfig.dbDriverClassName)
        dataSource.setUrl(dbConfig.dburl)
        dataSource.setUsername(dbConfig.dbUser)
        dataSource.setPassword(dbConfig.dbPassword)
        dataSource.setInitialSize(dbConfig.dbPoolSizeInitial)
        dataSource.setMaxTotal(dbConfig.dbPoolSizeMax)
        this.dataSource = dataSource
    }

    def getSql() {
        def get = sql.get()
        try {
            if (dbVendor == ORACLE) {
                get.execute("SELECT 1 FROM dual")
            } else {
                get.execute("SELECT 1")
            }
        } catch (Throwable throwable) {
            sql.remove()
        }
        sql.get()
    }

    def closeSqlConnection() {
        sql = null
        ((BasicDataSource) dataSource).close()
        executorService.shutdown()
        executorService.awaitTermination(2, TimeUnit.MINUTES)
    }

    def setVersion(String version) {
        databaseHelper.setVersion(version)
    }

    def <T> Future<T> asyncExec(Callable<T> callable) {
        executorService.submit(callable)
    }

}

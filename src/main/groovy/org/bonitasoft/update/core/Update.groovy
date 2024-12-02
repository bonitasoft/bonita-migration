/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.update.core
/**
 *
 * Get all versions and steps to execute and launch the update runner with it
 *
 * @author Baptiste Mesta
 */
class Update {
    private static final Logger logger = new Logger()
    private UpdateContext context
    private DisplayUtil displayUtil

    // for testing only
    Update(UpdateContext context, DisplayUtil displayUtil) {
        this.context = context
        this.displayUtil = displayUtil
    }

    static void main(String[] args) {
        startApplication(args, false)
    }

    static void startApplication(String[] args, boolean isSp) {
        def arguments = parseArguments(args)
        try {
            def updateContext = new UpdateContext(logger: logger)
            updateContext.verifyOnly = arguments.verify
            updateContext.updateIndexes = arguments.updateIndexes
            new Update(
                    updateContext,
                    new DisplayUtil(logger: logger)
                    ).run(isSp)
        } catch (Throwable ignored) {
            // logs managed in the run method
            System.exit(-1)
        }
    }

    private static UpdateArguments parseArguments(String[] args) {
        try {
            UpdateArguments arguments = UpdateArguments.parse(args)
            if (arguments.printHelp) {
                UpdateArguments.printHelp()
                System.exit(-1)
            }
            return arguments
        } catch (Exception e) {
            logger.error("Invalid command line: " + e.getMessage())
            UpdateArguments.printHelp()
            System.exit(-1)
        }
    }

    void run(boolean isSp) {
        try {
            def runner = createRunner()
            context.start()
            logUpdateBannerAndGlobalWarnings(isSp, runner)
            logJvmInformation()
            context.loadConfiguration()
            logJdbcDriverInformation()

            connectToDatabase()
            try {
                runner.run(isSp)
            }
            finally {
                context.closeSqlConnection()
            }
        } catch (Throwable t) {
            logger.error(t.getMessage())
            logger.debug('', t)
            throw t
        }
    }

    protected UpdateAction createRunner() {
        if (context.verifyOnly) {
            return new UpdateVerifier(context: context, logger: logger, displayUtil: displayUtil)
        } else if (context.updateIndexes) {
            return new UpdateIndexesOnly(context: context, logger: logger, displayUtil: displayUtil)
        } else {
            return new UpdateRunner(context: context, logger: logger, displayUtil: displayUtil)
        }
    }

    private void connectToDatabase() {
        def dbVendor = context.dbVendor
        logger.info "Gathering Database Information"
        context.openSqlConnection()

        List<String> databaseInformation = UpdateUtil.getDatabaseInformation(context.sql, dbVendor)
        if (!databaseInformation.empty) {
            logger.info 'Database Information'
            databaseInformation.each { logger.info "  ${it}" }
        }
    }

    private Properties getProjectProperties() {
        def properties = new Properties()
        this.class.getResourceAsStream("/bonita-update-info.properties").withStream {
            properties.load(it)
        }
        return properties
    }

    def logUpdateBannerAndGlobalWarnings(boolean isSp, UpdateAction runner) {
        def updateToolVersion = getProjectProperties().getProperty("update.tool.version", "DEV")
        def banner = (["", "Bonita update tool ${updateToolVersion} ${Edition.from(isSp).displayName} edition", ""] +
        runner.getBannerAndGlobalWarnings() + [""]) as String[]
        displayUtil.logInfoCenteredInRectangle(banner)
    }

    private static logJvmInformation() {
        def sysProps = System.getProperties()
        logger.info "JVM Information"
        logger.info "  java.version ${sysProps['java.version']}"
        logger.info "  java.runtime.version ${sysProps['java.runtime.version']}"
        logger.info "  java.vendor ${sysProps['java.vendor']}"
        logger.info "  java.vm.name ${sysProps['java.vm.name']}"
        logger.info "  java.vm.vendor ${sysProps['java.vm.vendor']}"
        logger.info "  os.name ${sysProps['os.name']}"
        logger.info "  os.arch ${sysProps['os.arch']}"
    }

    private logJdbcDriverInformation() {
        String driverClassName = context.dbConfig?.dbDriverClassName
        if (!driverClassName) {
            // not provided, mainly in tests, so skip
            return
        }
        logger.info "Jdbc Driver Information"
        logger.info "  driver ${driverClassName}"
        def version = Class.forName(driverClassName).getPackage().implementationVersion
        if (!version) {
            // MSSQL Server case
            // We may read the Bundle-Version attribute in the Manifest
            version = 'N/A'
        }
        logger.info "  implementation-version ${version}"
    }
}

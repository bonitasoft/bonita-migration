package org.bonitasoft.migration.plugin

import org.bonitasoft.migration.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.tasks.JavaExec
/**
 * @author Emmanuel Duchastenier
 */
class RunMsSqlserverXARecoveryTask extends JavaExec {

    @Override
    void exec() {
        def testValues = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
        logger.info "Current project: $project.name"

        def toolSystemProperties = [:]
        toolSystemProperties.put('host', testValues['db.server.name'])
        toolSystemProperties.put('port', testValues['db.server.port'])
        toolSystemProperties.put('database', testValues['db.database.name'])
        toolSystemProperties.put('user', testValues['db.user'])
        toolSystemProperties.put('password', testValues['db.password'])

        systemProperties toolSystemProperties
        logger.info "Calling MS SQL Server XARecovery tool (to initialize MSDTC module) using system properties $systemProperties"
        logger.debug "using classpath:"
        classpath(project.getConfigurations().getByName("xarecovery"))
        super.exec()
    }

}

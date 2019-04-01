package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.TestLogger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Blob

/**
 * @author Danila Mazour
 */
class MigrateEmailConnectorTest extends Specification {

    @Shared
    TestLogger logger = new TestLogger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    ConnectorTestUtils utils = new ConnectorTestUtils(migrationContext: migrationContext)

    UpdateConnectorDefinitionsForJava11 migrationStep = new UpdateConnectorDefinitionsForJava11()

    def setup() {
        dbUnitHelper.createTables("7_9_0/connectors")
        migrationContext.sql.executeInsert("""INSERT INTO sequence (tenantid,id,nextid) VALUES (1,10,1)""")
        migrationContext.sql.executeInsert("""INSERT INTO sequence (tenantid,id,nextid) VALUES (1,11,1)""")
    }

    def cleanup(){
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["process_definition"] as String[])
        dbUnitHelper.dropTables(["bar_resource"] as String[])
        dbUnitHelper.dropTables(["dependencymapping"] as String[])
        dbUnitHelper.dropTables(["dependency"] as String[])
        dbUnitHelper.dropTables(["sequence"] as String[])
    }

    def "should change all the outdated email connectors to the new version"() {
        given:

        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('email-1.0.12.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-email-impl-1.0.12.jar', 5196158028417589387)
        utils.insertConnectorDependency('mail-1.4.5.jar', 5196158028417589387)


        utils.createProcessDefinition(5924936294169136126, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('email-1.0.2.impl', 5924936294169136126)
        utils.insertConnectorDependency('bonita-connector-email-impl-6.1.0.jar', 5924936294169136126)
        utils.insertConnectorDependency('mail-1.4.5.jar', 5924936294169136126)

        when:
        migrationStep.execute(migrationContext)

        then:
        def dependencyFirstProcess = utils.getDependenciesOfProcess(5196158028417589387)
        def dependencySecondProcess = utils.getDependenciesOfProcess(5924936294169136126)
        dependencyFirstProcess.filename == ['bonita-connector-email-1.1.0.jar', 'mail-1.4.7.jar']
        dependencyFirstProcess.value_.collect{(it instanceof Blob)?it.binaryStream.bytes:it} == [utils.getJarFile('email/bonita-connector-email-1.1.0.jar'), utils.getJarFile('email/mail-1.4.7.jar')]
        dependencySecondProcess.filename == ['bonita-connector-email-1.1.0.jar', 'mail-1.4.7.jar']
        dependencySecondProcess.value_.collect{(it instanceof Blob)?it.binaryStream.bytes:it} == [utils.getJarFile('email/bonita-connector-email-1.1.0.jar'), utils.getJarFile('email/mail-1.4.7.jar')]

        utils.getContentResourcesOfProcess(5196158028417589387) == [utils.getContentOfImplFile('email-1.1.0.impl')]
        utils.getContentResourcesOfProcess(5924936294169136126) == [utils.getContentOfImplFile('email-1.1.0.impl')]
    }

    def "should leave email connector untouched if the version is high enough"() {
        given:

        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('email-1.1.0.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-email-1.1.0.jar', 5196158028417589387)
        utils.insertConnectorDependency('mail-1.4.7.jar', 5196158028417589387)

        when:
        migrationStep.execute(migrationContext)

        then:
        def dependencyProcess = utils.getDependenciesOfProcess(5196158028417589387).filename
        dependencyProcess == ['bonita-connector-email-1.1.0.jar', 'mail-1.4.7.jar']
        utils.getContentResourcesOfProcess(5196158028417589387) == [utils.getContentOfImplFile('email-1.1.0.impl')]

    }

    def "should not modify anything if mail-1.45 dependency is in an other connector"() {
        given:

        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('email-1.0.12.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-email-1.0.12.jar', 5196158028417589387)
        utils.insertConnectorDependency('mail-1.4.5.jar', 5196158028417589387)

        // depends on mail-1.4.5
        utils.insertConnectorImplementation('scripting-groovy-script-impl-1.0.2.impl', 5196158028417589387)

        when:
        migrationStep.execute(migrationContext)

        then:
        def dependencyProcess = utils.getDependenciesOfProcess(5196158028417589387).filename
        dependencyProcess == ['bonita-connector-email-1.0.12.jar', 'mail-1.4.5.jar']
        utils.getContentResourcesOfProcess(5196158028417589387) ==
                [utils.getContentOfImplFile('email-1.0.12.impl'),
                 utils.getContentOfImplFile('scripting-groovy-script-impl-1.0.2.impl')]

    }

    def "should produce warnings when connector is not migrated due to dependency used by an other connector"() {
        given:

        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('email-1.0.12.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-email-1.0.12.jar', 5196158028417589387)
        utils.insertConnectorDependency('mail-1.4.5.jar', 5196158028417589387)

        // depends on mail-1.4.5
        utils.insertConnectorImplementation('scripting-groovy-script-impl-1.0.2.impl', 5196158028417589387)
        logger.clear()
        when:
        migrationStep.execute(migrationContext)

        then:
        logger.warnLogs.join("\n") == """  Unable to migrate connector(s) [email-impl] to version 1.1.0:
    dependency mail-1.4.5.jar is already used by:
    - scripting-groovy-script-impl in version 1.0.2"""
        migrationStep.warning.contains("We could not migrate some connectors.")
    }

    def "should not produce warnings when connector is migrated correctly"() {
        given:

        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('email-1.0.12.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-email-1.0.12.jar', 5196158028417589387)
        utils.insertConnectorDependency('mail-1.4.5.jar', 5196158028417589387)
        logger.clear()
        when:
        migrationStep.execute(migrationContext)

        then:
        logger.infoLogs.join("\n") == """Migration of connectors of process 'My Process' in version 1.0.0:
  Connector(s) [email-impl] migrated to version 1.1.0:
    added
    - bonita-connector-email-1.1.0.jar
    - mail-1.4.7.jar
    removed
    - mail-1.4.5.jar
    - bonita-connector-email-impl-1.0.12.jar"""
        logger.warnLogs.empty
        migrationStep.warning == null
    }

}

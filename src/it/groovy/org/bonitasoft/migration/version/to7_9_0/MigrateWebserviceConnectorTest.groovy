package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class MigrateWebserviceConnectorTest extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def jarDependencyList = ['bonita-connector-webservice-1.2.2.jar',
                                            'gmbal-api-only-3.1.0-b001.jar',
                                            'ha-api-3.1.8.jar',
                                            'javax.xml.soap-api-1.3.8.jar',
                                            'jaxws-api-2.2.7.jar',
                                            'jaxws-rt-2.2.7.jar',
                                            'jsr181-api-1.0-MR1.jar',
                                            'management-api-3.0.0-b012.jar',
                                            'mimepull-1.9.7.jar',
                                            'policy-2.3.1.jar',
                                            'relaxngDatatype-20020414.jar',
                                            'saaj-api-1.3.4.jar',
                                            'saaj-impl-1.3.28.jar',
                                            'stax-ex-1.7.8.jar',
                                            'streambuffer-1.5.jar',
                                            'txw2-20110809.jar']

    ConnectorTestUtils utils = new ConnectorTestUtils(migrationContext: migrationContext)

    UpdateConnectorDefinitionsForJava11 migrationStep = new UpdateConnectorDefinitionsForJava11()

    def setup() {
        dropTestTables()
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

    def "should change all the outdated webService connectors to the new version"() {

        given:
        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('webservice-1.1.0.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-webservice-impl-1.1.0.jar', 5196158028417589387)

        utils.createProcessDefinition(5924936294169136126, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('webservice-1.0.11.impl', 5924936294169136126)
        utils.insertConnectorDependency('bonita-connector-webservice-impl-6.1.0.jar', 5924936294169136126)
        utils.insertConnectorDependency('jsr181-api-1.0-MR1.jar', 5924936294169136126)

        utils.createProcessDefinition(5964546, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('scripting-groovy-script-impl-1.0.2.impl', 5964546)
        utils.insertConnectorDependency('bonita-connector-custom-0.6.2.jar', 5964546)

        when:
        migrationStep.execute(migrationContext)

        then:

        def dependencyFirstProcess = utils.getDependenciesOfProcess(5196158028417589387).filename
        def dependencySecondProcess = utils.getDependenciesOfProcess(5924936294169136126).filename
        def dependencyThirdProcess = utils.getDependenciesOfProcess(5964546).filename
        dependencyFirstProcess == jarDependencyList
        dependencySecondProcess == ['bonita-connector-webservice-impl-6.1.0.jar', 'jsr181-api-1.0-MR1.jar']
        dependencyThirdProcess == ['bonita-connector-custom-0.6.2.jar']

        utils.getContentResourcesOfProcess(5196158028417589387) == [utils.getContentOfImplFile('webservice-1.2.2.impl')]
        utils.getContentResourcesOfProcess(5924936294169136126) == [utils.getContentOfImplFile('webservice-1.0.11.impl')]
        utils.getContentResourcesOfProcess(5964546) == [utils.getContentOfImplFile('scripting-groovy-script-impl-1.0.2.impl')]
    }

    def "should leave webService connector untouched if the version is high enough"() {

        given:
        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('webservice-1.2.2.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-webservice-1.2.2.jar',5196158028417589387)

        when:
        migrationStep.execute(migrationContext)

        then:
        def dependencyProcess = utils.getDependenciesOfProcess(5196158028417589387)
        // didn't change anything
        dependencyProcess.filename == ['bonita-connector-webservice-1.2.2.jar']
        utils.getContentResourcesOfProcess(5196158028417589387) == [utils.getContentOfImplFile('webservice-1.2.2.impl')]
    }

    def "should update sequence table with new nextid" () {

        given:
        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('webservice-1.1.0.impl', 5196158028417589387)
        utils.insertConnectorDependency('bonita-connector-webservice-1.1.0.jar', 5196158028417589387)

        utils.createProcessDefinition(5924936294169136126, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('webservice-1.0.11.impl', 5924936294169136126)
        utils.insertConnectorDependency('bonita-connector-webservice-1.0.11.jar', 5924936294169136126)

        when:
        migrationStep.execute(migrationContext)

        then:
        utils.getNextIdOfSequence(10) > migrationContext.sql.firstRow("""SELECT MAX(id) FROM dependency""")[0]
        utils.getNextIdOfSequence(11) > migrationContext.sql.firstRow("""SELECT MAX(id) FROM dependencymapping""")[0]
    }
}

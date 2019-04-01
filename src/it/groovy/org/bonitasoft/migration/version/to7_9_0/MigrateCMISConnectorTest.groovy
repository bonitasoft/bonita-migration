package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Danila Mazour
 */
class MigrateCMISConnectorTest extends Specification {

    @Shared
    Logger logger = new Logger()

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

    def "should change one cmis connectors to the new version with all dependencies"() {
        given:
        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('cmis-createfolder-2.0.1.impl', 5196158028417589387)

        def impl = ConnectorImplementation.parseConnectorXml(new String(utils.getContentOfImplFile('cmis-createfolder-2.0.1.impl')))
        impl.jarDependencies.each {
            utils.insertConnectorDependency(it, 5196158028417589387)
        }

        when:
        migrationStep.execute(migrationContext)

        then:
        def theNewImpl = ConnectorImplementation.parseConnectorXml(new String(utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl')))

        utils.getDependenciesOfProcess(5196158028417589387).filename.toSorted() == theNewImpl.jarDependencies.toSorted()
        utils.getContentResourcesOfProcess(5196158028417589387) == [utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl')]
    }

    def "should change two cmis connectors to the new version with all dependencies"() {
        given:
        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('cmis-createfolder-2.0.1.impl', 5196158028417589387)
        utils.insertConnectorImplementation('cmis-deletedocument-2.0.1.impl', 5196158028417589387)

        def impl = ConnectorImplementation.parseConnectorXml(new String(utils.getContentOfImplFile('cmis-createfolder-2.0.1.impl')))
        impl.jarDependencies.each {
            utils.insertConnectorDependency(it, 5196158028417589387)
        }

        when:
        migrationStep.execute(migrationContext)

        then:
        def theNewCreateFolderImpl = ConnectorImplementation.parseConnectorXml(new String(utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl')))

        utils.getDependenciesOfProcess(5196158028417589387).filename.toSorted() == theNewCreateFolderImpl.jarDependencies.toSorted()
        utils.getContentResourcesOfProcess(5196158028417589387) == [
                utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl'),
                utils.getContentOfImplFile('cmis-deletedocument-3.0.3.impl')
        ]
    }

    def "should update old implementation of cmis connector but keep already up to date implementation"() {
        given:
        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('cmis-createfolder-2.0.1.impl', 5196158028417589387)
        utils.insertConnectorImplementation('cmis-deletedocument-3.0.3.impl', 5196158028417589387)

        def allDependencies = parseConnector('cmis-createfolder-2.0.1.impl').jarDependencies + parseConnector('cmis-deletedocument-3.0.3.impl').jarDependencies
        allDependencies.toSet().each {
            utils.insertConnectorDependency(it, 5196158028417589387)
        }

        when:
        migrationStep.execute(migrationContext)

        then:
        def theNewCreateFolderImpl = ConnectorImplementation.parseConnectorXml(new String(utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl')))

        utils.getDependenciesOfProcess(5196158028417589387).filename.toSorted() == theNewCreateFolderImpl.jarDependencies.toSorted()
        utils.getContentResourcesOfProcess(5196158028417589387) == [
                utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl'),
                utils.getContentOfImplFile('cmis-deletedocument-3.0.3.impl')
        ]
    }

    def "should update old implementation of cmis connector when process already have some existing dependencies"() {
        given:
        utils.createProcessDefinition(5196158028417589387, 'My Process', '1.0.0')
        utils.insertConnectorImplementation('cmis-createfolder-2.0.1.impl', 5196158028417589387)

        parseConnector('cmis-createfolder-2.0.1.impl').jarDependencies.each {
            utils.insertConnectorDependency(it, 5196158028417589387)
        }
        def existingDependencies = parseConnector('cmis-deletedocument-3.0.3.impl').jarDependencies[0..3]
        existingDependencies.each { utils.insertConnectorDependency(it, 5196158028417589387) }

        when:
        migrationStep.execute(migrationContext)

        then:
        def theNewCreateFolderImpl = ConnectorImplementation.parseConnectorXml(new String(utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl')))

        utils.getDependenciesOfProcess(5196158028417589387).filename.toSorted() == theNewCreateFolderImpl.jarDependencies.toSorted()
        utils.getContentResourcesOfProcess(5196158028417589387) == [
                utils.getContentOfImplFile('cmis-createfolder-3.0.3.impl')
        ]
    }

    private ConnectorImplementation parseConnector(String s) {
        ConnectorImplementation.parseConnectorXml(new String(utils.getContentOfImplFile(s)))
    }

}

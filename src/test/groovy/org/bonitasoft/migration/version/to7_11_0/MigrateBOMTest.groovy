package org.bonitasoft.migration.version.to7_11_0

import static org.bonitasoft.migration.core.IOUtil.unzip
import static org.bonitasoft.migration.core.IOUtil.zip

import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import spock.lang.Specification

class MigrateBOMTest extends Specification {
    private Sql sql

    def 'should add namespace to BOM.xml'() {
        given:
        def migrationStep = new MigrateBOM()
        def migrationContext = newMigrationContext()

        when:
        def migratedClientBDMZip = migrationStep.migrateBOM(zip(["bom.zip": zip(["bom.xml": "<businessObjectModel></businessObjectModel>".bytes])]), migrationContext, 12, 1)
        then:
        new String(unzip(unzip(migratedClientBDMZip)."bom.zip")."bom.xml") == "<businessObjectModel xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\"></businessObjectModel>"
    }

    def 'should not migrate when namespace is already here'() {
        given:
        def migrationStep = new MigrateBOM()
        def migrationContext = newMigrationContext()

        when:
        def migratedClientBDMZip = migrationStep.migrateBOM(zip(["bom.zip": zip(["bom.xml": "<businessObjectModel xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\"></businessObjectModel>".bytes])]), migrationContext, 12, 1)
        then:
        migratedClientBDMZip == null // it means nothing changed
    }

    def 'should not migrate a second time the same bom.xml'() {
        given:
        def migrationStep = new MigrateBOM()
        def migrationContext = newMigrationContext()

        when:
        def migratedClientBDMZip = migrationStep.migrateBOM(migrationStep.migrateBOM(zip(["bom.zip": zip(["bom.xml": "<businessObjectModel></businessObjectModel>".bytes])]), migrationContext, 12, 1), migrationContext, 12, 1)
        then:
        migratedClientBDMZip == null // it means nothing changed
    }


    private MigrationContext newMigrationContext() {
        def migrationContext = Mock(MigrationContext)
        migrationContext.databaseHelper >> Mock(DatabaseHelper)
        migrationContext.logger >> Mock(Logger)
        sql = Mock(Sql)
        migrationContext.sql >> sql
        migrationContext
    }
}

package org.bonitasoft.migration.version.to7_3_1

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Baptiste Mesta
 */
class FixDependenciesNameIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        migrationContext.setVersion("7.3.1")
        dropTables()
        dbUnitHelper.createTables("7_3_1/dependencies")
        migrationContext.bonitaHome = temporaryFolder.newFolder()

    }

    def cleanup() {
        dropTables()
    }

    private String[] dropTables() {
        dbUnitHelper.dropTables(["dependency"] as String[])
    }

    def "should replace slashes in the dependency name and filename"() {
        given:
        def byte[] content = [1, 2]
        migrationContext.sql.execute("INSERT INTO dependency (tenantid, id, name, description, filename, value_) VALUES (${1L},${302L}, ${'5335973144527898509_providedscripts.jar'}, ${null},${'providedscripts.jar'},${content})")
        migrationContext.sql.execute("INSERT INTO dependency (tenantid, id, name, description, filename, value_) VALUES (${2L},${306L}, ${'5335973144527898509_/ojdbc6.jar'}, ${null},${'/ojdbc6.jar'},${content})")
        when:
        new FixDependenciesName().execute(migrationContext)
        then:
        assert (migrationContext.sql.firstRow("SELECT name, filename FROM dependency WHERE id = ${302L}") as Map).collectEntries { key, value ->
            [key.toString().toLowerCase(), value]
        } == [name: '5335973144527898509_providedscripts.jar', filename: 'providedscripts.jar']
        assert (migrationContext.sql.firstRow("SELECT name, filename FROM dependency WHERE id = ${306L}") as Map).collectEntries { key, value ->
            [key.toString().toLowerCase(), value]
        } == [name: '5335973144527898509_ojdbc6.jar', filename: 'ojdbc6.jar']
    }

}

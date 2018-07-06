package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class FixJarJarDependencyNameIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        migrationContext.setVersion("7.3.1") // reuse sql files from version 7.3.1
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

    def "should replace .jar.jar by .jar in the dependency filename"() {
        given:
        byte[] content = [1, 2]
        migrationContext.sql.execute("INSERT INTO dependency (tenantid, id, name, description, filename, value_) VALUES (1,100, '533597317898509_myDep1.jar', ${null},${'myDep1.jar.jar'},${content})")
        migrationContext.sql.execute("INSERT INTO dependency (tenantid, id, name, description, filename, value_) VALUES (2,101, '533597314452509_myDep2.jar', ${null},${'myDep2.jar'},${content})")
        migrationContext.sql.execute("INSERT INTO dependency (tenantid, id, name, description, filename, value_) VALUES (2,102, '533597311111111_SMTHG.JAR', ${null},${'SMTHG.JAR.jar'},${content})")
        migrationContext.sql.execute("INSERT INTO dependency (tenantid, id, name, description, filename, value_) VALUES (1,103, '44444_classes12.ZIP', ${null},${'classes12.ZIP.jar'},${content})")
        migrationContext.sql.execute("INSERT INTO dependency (tenantid, id, name, description, filename, value_) VALUES (1,300, '5555_my.custom.dependency.jar', ${null},${'my.custom.dependency.jar'},${content})")

        when:
        new FixJarJarDependencyName().execute(migrationContext)

        then:
        List allRows = migrationContext.sql.rows("SELECT filename FROM dependency ORDER BY id ASC")
        allRows.size() == 5

        allRows[0].filename == 'myDep1.jar'
        allRows[1].filename == 'myDep2.jar'
        allRows[2].filename == 'SMTHG.JAR'
        allRows[3].filename == 'classes12.ZIP'
        allRows[4].filename == 'my.custom.dependency.jar'
    }

}

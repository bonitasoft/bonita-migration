package org.bonitasoft.migration.version.to7_3_0

import groovy.sql.Sql
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.database.DatabaseHelper
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author Baptiste Mesta
 */
public class TenantResourcesTest extends Specification {

    def context = Mock(MigrationContext)

    def logger = Mock(Logger)

    def sql = Mock(Sql)

    def databaseHelper = Mock(DatabaseHelper)

    @Rule
    TemporaryFolder temporaryFolder

    def setup() {
        databaseHelper.sql >> sql
        context.sql >> sql
        context.databaseHelper >> databaseHelper
        context.logger >> logger
    }


    public void "migrateClientBDMZip should insert row in database when client-bdm.zip exists"() throws Exception {
        given:
        def tenantResources = new TenantResources()
        def folder = temporaryFolder.newFolder()
        def bdmZip = new File(new File(folder,"data-management-client"),"client-bdm.zip")
        bdmZip.parentFile.mkdirs()
        bdmZip.bytes = "bdm client zip content".bytes
        def map = [12L: 104L]
        when:
        tenantResources.migrateClientBDMZip(context,  map, folder, 12L)

        then:
        1 * context.sql.executeInsert("INSERT INTO tenant_resource VALUES (${12L},${104L},${"client-bdm.zip"},${"BDM"},${"bdm client zip content".bytes})")
        map.get(12L) == 105L
        !bdmZip.exists()
    }

    public void "migrateClientBDMZip should do nothing when client-bdm.zip do not exists"() throws Exception {
        given:
        def tenantResources = new TenantResources()
        def folder = temporaryFolder.newFolder()
        def map = [12L: 104L]
        when:
        tenantResources.migrateClientBDMZip(context,  map, folder, 12L)

        then:
        0 * context.sql.executeInsert(_ as GString)
        0 * context.sql.executeInsert(_ as String)
    }
}
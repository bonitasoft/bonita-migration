/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_13_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 * @author Dumitru Corini
 */
class CreateNewPagesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    private CreateNewPages migrationStep = new CreateNewPages()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_13_0/pages")
        migrationContext.sql.executeInsert("""INSERT INTO sequence (tenantid,id,nextid) VALUES (1,10120,54)""")
        migrationContext.sql.executeInsert("""INSERT INTO sequence (tenantid,id,nextid) VALUES (8,10120,54)""")
        migrationContext.sql.executeInsert("""INSERT INTO sequence (tenantid,id,nextid) VALUES (10,10120,937)""")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page", "sequence", "tenant"] as String[])
    }


    def "should create all new removable pages as provided pages"() {
        given:
        migrationContext.sql.executeInsert("INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                , 8L, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                'default', 'ACTIVATED')
        migrationContext.sql.executeInsert("INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                , 10L, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                'default', 'ACTIVATED')
        assert 0 == migrationContext.sql.firstRow("""SELECT count(id) FROM page 
            WHERE name IN ('custompage_userCaseDetailsBonita', 'custompage_userCaseListBonita')""")[0]

        when:
        migrationStep.execute(migrationContext)

        then:
        // all but one lines should have been updated:
        def rows = migrationContext.sql.rows("""SELECT tenantId, name, content
            FROM page 
            WHERE name IN ('custompage_userCaseDetailsBonita', 'custompage_userCaseListBonita')
            AND provided = ${true}
            ORDER BY tenantId, name""")
        rows.size() == 4

        rows[0].tenantId == 8L
        rows[0].name == 'custompage_userCaseDetailsBonita'
        migrationContext.databaseHelper.getBlobContentAsString(rows[0].content) == 'will be updated'

        rows[1].tenantId == 8L
        rows[1].name == 'custompage_userCaseListBonita'
        migrationContext.databaseHelper.getBlobContentAsString(rows[1].content) == 'will be updated'

        rows[2].tenantId == 10L
        rows[2].name == 'custompage_userCaseDetailsBonita'
        migrationContext.databaseHelper.getBlobContentAsString(rows[2].content) == 'will be updated'
        rows[3].tenantId == 10L
        rows[3].name == 'custompage_userCaseListBonita'
        migrationContext.databaseHelper.getBlobContentAsString(rows[3].content) == 'will be updated'
    }


    def "should skip pages that already exists"() {
        given:
        def tenantId = 8L
        def currentTimeMillis = System.currentTimeMillis()
        migrationContext.with {
            sql.executeInsert("INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                    , tenantId, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                    'default', 'ACTIVATED')
            sql.executeInsert("""INSERT INTO page(tenantId , id, name, displayName, description, installationDate, 
installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId, hidden)
VALUES (${tenantId}, ${databaseHelper.getAndUpdateNextSequenceId(10120L, tenantId)}, 'custompage_userCaseDetailsBonita', 'will be updated', '', ${currentTimeMillis}, 
-1, ${false}, ${currentTimeMillis}, -1, 'userCaseDetailsBonita.zip', ${''.getBytes()}, '', 0, ${false})""")
        }

        when:
        migrationStep.execute(migrationContext)

        then:
        // all but one lines should have been updated:
        def rows = migrationContext.sql.rows("""SELECT tenantId, name, provided
            FROM page 
            WHERE name IN ('custompage_userCaseDetailsBonita', 'custompage_userCaseListBonita')
            ORDER BY tenantId, name""")
        rows.size() == 2

        rows[0].tenantId == tenantId
        rows[0].name == 'custompage_userCaseDetailsBonita'
        !rows[0].provided // we keep what was in db
        rows[1].tenantId == tenantId
        rows[1].name == 'custompage_userCaseListBonita'
        rows[1].provided
    }

    def "should update all final pages with flags to false"() {
        given:
        def tenantId = 1L
        def currentTimeMillis = System.currentTimeMillis()
        migrationContext.with {
            sql.executeInsert("INSERT INTO tenant (id, created, createdby, description, defaulttenant, iconname, iconpath, name, status) VALUES (?,?,?,?,?,?,?,?,?)"
                    , tenantId, 1452271739683, 'defaultUser', 'Default tenant', dbUnitHelper.falseValue(), null, null,
                    'default', 'ACTIVATED')
        }
        def pages = ['custompage_themeBonita', 'custompage_caseoverview', 'custompage_layoutBonita', 'custompage_adminApplicationDetailsBonita',
                     'custompage_adminApplicationListBonita', 'custompage_adminBDMBonita', 'custompage_adminCaseListBonita', 'custompage_adminCaseVisuBonita',
                     'custompage_adminInstallExportOrganizationBonita', 'custompage_adminLicenseBonita', 'custompage_adminMonitoringBonita',
                     'custompage_adminProcessDetailsBonita', 'custompage_adminProcessVisuBonita', 'custompage_adminUserDetailsBonita', 'custompage_home',
                     'custompage_tenantStatusBonita', 'custompage_processAutogeneratedForm', 'custompage_taskAutogeneratedForm']
        def i = 0
        byte[] content = "myValue".getBytes()
        for (String page: pages) {
            migrationContext.sql.execute("""
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId, editable, removable, hidden)
values (1, ${++i}, ${page}, 'page 1', 'my first page', 1, 1, ${dbUnitHelper.falseValue()}, 1, 1, 'a page', $content, 'a content', 1, ${true}, ${true}, ${false})
""")
        }
        // insert another page that should not be updated:
        migrationContext.sql.execute("""
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId, editable, removable, hidden)
values (1, 292, 'custompage_shouldNotBeDeleted', 'page 1', 'my first page', 1, 1, ${dbUnitHelper.falseValue()}, 1, 1, 'a page', $content, 'a content', 1, ${true}, ${true}, ${false})
""")
        assert 19 == migrationContext.sql.firstRow("SELECT count(id) FROM page") [0]

        when:
        // we need to have
        migrationStep.execute(migrationContext)

        then:
        // all but one lines should have been updated:
        27 == migrationContext.sql.firstRow("SELECT count(id) FROM page WHERE removable = ${dbUnitHelper.falseValue()} AND editable = ${dbUnitHelper.falseValue()}") [0]
        1 == migrationContext.sql.firstRow("SELECT count(id) FROM page WHERE name = 'custompage_shouldNotBeDeleted' AND editable = ${true} AND removable = ${true}") [0]
    }
}

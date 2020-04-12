/**
 * Copyright (C) 2020 BonitaSoft S.A.
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
package org.bonitasoft.migration

import org.bonitasoft.migration.core.*
import spock.lang.Shared
import spock.lang.Specification

import static org.bonitasoft.migration.core.MigrationUtil.AUTO_ACCEPT

/**
 * @author Danila Mazour
 */
class UpdateV6CaseOverviewIT extends Specification {
    @Shared
    Logger logger = new Logger()

    @Shared
    DisplayUtil displayUtil = new DisplayUtil()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)
    MigrationAction updateV6CaseOverview = new UpdateV6CaseOverview(logger: logger, displayUtil: displayUtil, context: migrationContext)

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_8_0/pages_and_forms")
    }

    def cleanup() {
        dropTestTables()
        System.clearProperty(AUTO_ACCEPT)
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page", "form_mapping", "page_mapping", "process_definition", "process_instance", "bar_resource", "process_definition", "tenant"] as String[])
    }


    def "should update case overview mappings for the given process definition"() {

        given:
        migrationContext.sql.executeInsert """INSERT INTO process_definition 
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1,1,6811359778547023483,'Pool1','1.0','',1583851869240,4,'ENABLED','RESOLVED','Pool 1','',1583851869371,null ,null ,1,3)"""
        migrationContext.sql.executeInsert """INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(2,2,6922460889658034964,'Pool2','1.5','',1583851869240,4,'ENABLED','RESOLVED','Pool 2','',1583851869371,null ,null ,2,4)"""
        // Case Overviews v6 page mapping that will be UPDATED to v7:
        migrationContext.sql.executeInsert """INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 40, 'processInstance/LeaveRequestCreation/7.7.4', NULL, NULL, 'legacy', NULL, 0, 0)"""
        // Case Overviews v6 page mapping that will not be UPDATED to v7, because it is of a different process:
        migrationContext.sql.executeInsert """INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(2, 498, 'processInstance/LeaveRequestUpdate/7.4.2', NULL, NULL, 'legacy', NULL, 0, 0)"""
        // Case Overview v6 form mappings that will be UPDATED to v7:
        migrationContext.sql.executeInsert """INSERT INTO form_mapping
(TENANTID, ID, PROCESS, type, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 34, 6811359778547023483, 2, NULL, 1, 40, 0, 0, 'LEGACY')"""
        // Case Overview v6 form mappings that will not be UPDATED to v7, because it is of a different process:
        migrationContext.sql.executeInsert """INSERT INTO form_mapping
(TENANTID, ID, PROCESS, type, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(2, 78, 6922460889658034964, 2, NULL, 2, 498, 0, 0, 'LEGACY')"""
        // the provided 'custompage_caseoverview' page:
        migrationContext.sql.executeInsert """
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
values (1, 99, 'custompage_caseoverview','page 1', 'my first page', 1, 1, ${dbUnitHelper.trueValue()}, 1, 1, 'a page',
${"myValue".getBytes()}, 'a content', 1)
"""
        // the provided 'custompage_caseoverview' page on tenant 2:
        migrationContext.sql.executeInsert """
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
values (2, 99, 'custompage_caseoverview','page 1', 'my first page', 1, 1, ${dbUnitHelper.trueValue()}, 1, 1, 'a page',
${"myValue".getBytes()}, 'a content', 1)
"""

        updateV6CaseOverview.processDefinition = 6811359778547023483
        displayUtil.setLogger(logger)
        System.setProperty(AUTO_ACCEPT, "true")

        when:
        updateV6CaseOverview.run(false)
        then:
        def formMappings = migrationContext.sql.rows("SELECT id, page_mapping_tenant_id, page_mapping_id, target, type FROM form_mapping ORDER BY id")
        formMappings.size() == 2

        formMappings.get(0).id == 34
        formMappings.get(0).page_mapping_tenant_id == 1
        formMappings.get(0).page_mapping_id == 40
        formMappings.get(0).target == 'INTERNAL'
        formMappings.get(0).type == 2

        formMappings.get(1).id == 78
        formMappings.get(1).page_mapping_tenant_id == 2
        formMappings.get(1).page_mapping_id == 498
        formMappings.get(1).type == 2
        formMappings.get(1).target == 'LEGACY'

        def pageMappings = migrationContext.sql.rows("SELECT pageid, urladapter, page_authoriz_rules, tenantid , id FROM page_mapping ORDER BY id")
        pageMappings.size() == 2
        // the updated first row
        pageMappings.get(0).pageid == 99 // this is the id of the provided 'custompage_caseoverview' page
        pageMappings.get(0).urladapter == null
        pageMappings.get(0).page_authoriz_rules == 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,'
        pageMappings.get(0).tenantid == 1
        pageMappings.get(0).id == 40

        // second row has not been updated
        pageMappings.get(1).urladapter == "legacy" //has not been updated
        pageMappings.get(1).id == 498
    }
}

/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_8_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class MigrateV6FormsIT extends Specification {

    @Shared
    Logger logger = new Logger()

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)
    MigrateV6Forms migrationStep = new MigrateV6Forms()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("7_8_0/pages_and_forms")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["page", "form_mapping", "page_mapping", "process_definition", "process_instance", "bar_resource", "tenant"] as String[])
    }

    def "should remove v6 form mappings with corresponding page mappings, update process resolution and update case overview mappings"() {

        given:
        // Add 2 tenants
        // using databaseHelper because of the boolean value
        migrationContext.databaseHelper.execute("""INSERT INTO tenant (ID,CREATED,CREATEDBY,DESCRIPTION,DEFAULTTENANT,ICONNAME,ICONPATH,NAME,STATUS) VALUES (1,13,'danila','first tenant',true,'iconname','iconpath','firsttenant','active')""")
        migrationContext.databaseHelper.execute("""INSERT INTO tenant (ID,CREATED,CREATEDBY,DESCRIPTION,DEFAULTTENANT,ICONNAME,ICONPATH,NAME,STATUS) VALUES (2,14,'danila','second tenant',false,'iconname','iconpath','secondtenant','active')""")
        // Page mappings that will be deleted by migration:
        migrationContext.sql.executeInsert """INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 38, 'process/LeaveRequestCreation/7.7.4', NULL, NULL, 'legacy', NULL, 0, 0)"""
        // Page mappings that will be deleted by migration:
        migrationContext.sql.executeInsert """INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(2, 64, 'process/LeaveRequestCreation/7.7.4', NULL, NULL, 'legacy', NULL, 0, 0)"""
        // the corresponding form mappings that will be updated by migration:
        migrationContext.sql.executeInsert """INSERT INTO form_mapping
(TENANTID, ID, PROCESS, type, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 32, 6811359778547023483, 1, NULL, 1, 38, 0, 0, 'LEGACY')"""
        migrationContext.sql.executeInsert """INSERT INTO form_mapping
(TENANTID, ID, PROCESS, type, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(2, 54, 6922460889658034964, 1, NULL, 2, 64, 0, 0, 'LEGACY')"""
        // a page mapping that will NOT be touched (because already form V7):
        migrationContext.sql.executeInsert """INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 39, 'taskInstance/LeaveRequestCreation/7.7.4/step1', 13, NULL, NULL, 'IS_ADMIN,IS_PROCESS_OWNER,IS_TASK_AVAILABLE_FOR_USER,', 0, 0)"""
        // the corresponding form mapping that will NOT be updated EITHER:
        migrationContext.sql.executeInsert """INSERT INTO form_mapping
(TENANTID, ID, PROCESS, type, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 33, 6811359778547023483, 3, NULL, 1, 39, 0, 0, 'INTERNAL')"""

        // Case Overviews v6 page mapping that will be UPDATED to v7:
        migrationContext.sql.executeInsert """INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(1, 40, 'processInstance/LeaveRequestCreation/7.7.4', NULL, NULL, 'legacy', NULL, 0, 0)"""
        migrationContext.sql.executeInsert """INSERT INTO page_mapping
(TENANTID, ID, KEY_, PAGEID, URL, URLADAPTER, PAGE_AUTHORIZ_RULES, LASTUPDATEDATE, LASTUPDATEDBY)
VALUES(2, 498, 'processInstance/LeaveRequestCreation/7.7.4', NULL, NULL, 'legacy', NULL, 0, 0)"""
        // Case Overview v6 form mappings that will be UPDATED to v7:
        migrationContext.sql.executeInsert """INSERT INTO form_mapping
(TENANTID, ID, PROCESS, type, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(1, 34, 6811359778547023483, 2, NULL, 1, 40, 0, 0, 'LEGACY')"""
        migrationContext.sql.executeInsert """INSERT INTO form_mapping
(TENANTID, ID, PROCESS, type, TASK, PAGE_MAPPING_TENANT_ID, PAGE_MAPPING_ID, LASTUPDATEDATE, LASTUPDATEDBY, TARGET)
VALUES(2, 78, 6922460889658034964, 2, NULL, 2, 498, 0, 0, 'LEGACY')"""
        // the corresponding process definition:
        migrationContext.sql.executeInsert """INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(1, 11, 6811359778547023483, 'LeaveRequestCreation', '7.7.4', '', 1538573628993, 4, 'DISABLED', 'RESOLVED', 'Leave Request Creation', '', 1538573629252, NULL, NULL, 1, 11)"""
        migrationContext.sql.executeInsert """INSERT INTO process_definition
(TENANTID, ID, PROCESSID, NAME, VERSION, DESCRIPTION, DEPLOYMENTDATE, DEPLOYEDBY, ACTIVATIONSTATE, CONFIGURATIONSTATE, DISPLAYNAME, DISPLAYDESCRIPTION, LASTUPDATEDATE, CATEGORYID, ICONPATH, CONTENT_TENANTID, CONTENT_ID)
VALUES(2, 642, 6922460889658034964, 'LeaveRequestCreation', '7.7.4', '', 1538573628993, 4, 'DISABLED', 'RESOLVED', 'Leave Request Creation', '', 1538573629252, NULL, NULL, 2, 11)"""

        // the provided 'custompage_caseoverview' page:
        migrationContext.sql.executeInsert """
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
values (1, 99, 'custompage_caseoverview','page 1', 'my first page', 1, 1, ${dbUnitHelper.trueValue()}, 1, 1, 'a page',
${"myValue".getBytes()}, 'a content', 1)
"""
        // the provided 'custompage_caseoverview' page on tenant 2:
        migrationContext.sql.executeInsert """
insert into page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
values (2, 777, 'custompage_caseoverview','page 1', 'my first page', 1, 1, ${dbUnitHelper.trueValue()}, 1, 1, 'a page',
${"myValue".getBytes()}, 'a content', 1)
"""
        // a normal v7 custom task form, that need to be present because of the foreign key from form_mapping:
        migrationContext.sql.executeInsert """
INSERT INTO page(tenantId , id, name, displayName, description, installationDate, installedBy, provided, lastModificationDate, lastUpdatedBy, contentName, content, contentType, processDefinitionId)
VALUES (1, 13, 'custompage_step1Form','page 1', 'step 1 form', 1, 1, ${dbUnitHelper.falseValue()}, 1, 1, 'a page',
${"someContent".getBytes()}, 'content type', 1)
"""
        // add forms v6 in bar resources:
        migrationContext.sql.executeInsert """INSERT INTO bar_resource (tenantid, id, process_id, name, type, content)
VALUES(1, 7, 6811359778547023483, 'forms/html/_confirm_template.html', 'EXTERNAL', ${"html content".getBytes()})"""

        migrationContext.sql.executeInsert """INSERT INTO bar_resource (tenantid, id, process_id, name, type, content)
VALUES(1, 3, 6811359778547023483, 'forms/forms.xml', 'EXTERNAL', ${"someContent".getBytes()})"""

        migrationContext.sql.executeInsert """INSERT INTO bar_resource (tenantid, id, process_id, name, type, content)
VALUES(1, 24, 6811359778547023483, 'process.bpmn', 'EXTERNAL', ${"xml content".getBytes()})"""

        when:
        migrationStep.execute(migrationContext)

        then:
        def formMappings = migrationContext.sql.rows("SELECT id, page_mapping_tenant_id, page_mapping_id, target, type FROM form_mapping ORDER BY id")
        formMappings.size() == 5
        formMappings.get(0).id == 32 // this line has benn updated
        formMappings.get(0).page_mapping_tenant_id == null
        formMappings.get(0).page_mapping_id == null
        formMappings.get(0).target == 'UNDEFINED'

        formMappings.get(1).id == 33 // this line has NOT been updated NOR deleted
        formMappings.get(1).page_mapping_tenant_id == 1
        formMappings.get(1).page_mapping_id == 39
        formMappings.get(1).target == 'INTERNAL'

        formMappings.get(2).id == 34
        formMappings.get(2).page_mapping_tenant_id == 1
        formMappings.get(2).page_mapping_id == 40
        formMappings.get(2).type == 2 // this is a case overview
        formMappings.get(2).target == 'INTERNAL' // the value has be updated

        formMappings.get(3).id == 54
        formMappings.get(3).page_mapping_tenant_id == null
        formMappings.get(3).page_mapping_id == null
        formMappings.get(3).target == 'UNDEFINED'

        formMappings.get(4).id == 78
        formMappings.get(4).page_mapping_tenant_id == 2
        formMappings.get(4).page_mapping_id == 498
        formMappings.get(4).target == 'INTERNAL'

        def pageMappings = migrationContext.sql.rows("SELECT pageid, urladapter, page_authoriz_rules, tenantid FROM page_mapping ORDER BY id")
        pageMappings.size() == 3
        // first row is not important because not updated.
        // second row:
        pageMappings.get(1).pageid == 99 // this is the id of the provided 'custompage_caseoverview' page
        pageMappings.get(1).urladapter == null
        pageMappings.get(1).page_authoriz_rules == 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,'
        pageMappings.get(1).tenantid == 1

        pageMappings.get(2).pageid == 777
        pageMappings.get(2).urladapter == null
        pageMappings.get(2).page_authoriz_rules == 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,'
        pageMappings.get(2).tenantid == 2

        def processDefinition = migrationContext.sql.rows("SELECT configurationstate FROM process_definition")
        processDefinition.size() == 2
        processDefinition.get(0).configurationstate == 'UNRESOLVED'
        processDefinition.get(1).configurationstate == 'UNRESOLVED'

        def bar_resources = migrationContext.sql.rows("SELECT * FROM bar_resource")
        bar_resources.size() == 1
        bar_resources.get(0).name == 'process.bpmn' // the only line that remains
    }

}

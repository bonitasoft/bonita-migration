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


import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Emmanuel Duchastenier
 */
class MigrateV6Forms extends MigrationStep {

    private static final String GET_TENANTS_IDS_ON_PLATFORM = """SELECT id FROM tenant"""

    private static final String FIND_ELEMENTS_TO_UPDATE = """SELECT DISTINCT pd.processid, f.page_mapping_tenant_id, f.page_mapping_id, f.tenantid, f.id
FROM process_definition pd, form_mapping f
WHERE pd.PROCESSID = f.PROCESS
AND pd.ACTIVATIONSTATE = 'DISABLED' -- ensure process is disabled before updating legacy form mappings
AND f.TARGET = 'LEGACY'
AND f.type <> 2"""

    private static final String UPDATE_V6_FORM_MAPPINGS = """UPDATE form_mapping
SET target = 'UNDEFINED',
    page_mapping_tenant_id=NULL,
    page_mapping_id=NULL
WHERE tenantid=? AND id=?"""
    private static final String DELETE_V6_PAGE_MAPPINGS = "DELETE FROM page_mapping WHERE tenantid = ? AND id = ?"

    private static final String UPDATE_PROCESS_RESOLUTION = """UPDATE process_definition
SET configurationstate = 'UNRESOLVED'
WHERE configurationstate = 'RESOLVED'
AND activationstate = 'DISABLED'
AND processid = ?"""

    private static final String UPDATE_CASE_OVERVIEW_PAGE_MAPPINGS = """UPDATE page_mapping
SET pageid = (SELECT id FROM page WHERE NAME = 'custompage_caseoverview' AND tenantid = ?),
    urladapter = null,
    page_authoriz_rules = 'IS_ADMIN,IS_PROCESS_OWNER,IS_PROCESS_INITIATOR,IS_TASK_PERFORMER,IS_INVOLVED_IN_PROCESS_INSTANCE,'
WHERE urladapter = 'legacy'
AND tenantid = ?
AND key_ like 'processInstance/%'""" // KEY_ like 'processInstance/%' means 'case overview'

    private static final String UPDATE_CASE_OVERVIEW_FORM_MAPPINGS = """UPDATE form_mapping SET target = 'INTERNAL' WHERE type = 2 AND target = 'LEGACY'"""

    private static final String DELETE_V6_FORMS_BAR_RESOURCE = "DELETE FROM bar_resource WHERE name LIKE 'forms/%'"

    @Override
    def execute(MigrationContext context) {
        setV6FormMappingsToV7Undefined(context)
        setAllCaseOverviewToV7(context)
        cleanUpAllV6FormsResources(context)
    }

    /**
     * Pre-migration check has already verified that only V6 form mappings existed (if any) on
     * DISABLED processes that had ONLY archived cases.
     * Here, the migration is executing, so we assume all pre-migration checks have been run.
     */
    private static setV6FormMappingsToV7Undefined(MigrationContext context) {
        context.sql.eachRow(FIND_ELEMENTS_TO_UPDATE) { row ->
            def pageMappingTenantId = row.page_mapping_tenant_id as long
            def pageMappingId = row.page_mapping_id as long
            def tenantId = row.tenantid as long
            def id = row.id as long
            def processDefId = row.processid as long
            // Order is important:
            // we need to update form mappings before removing page mappings, because of the foreign key:
            context.sql.executeUpdate(UPDATE_V6_FORM_MAPPINGS, [tenantId, id])
            context.sql.executeUpdate(DELETE_V6_PAGE_MAPPINGS, [pageMappingTenantId, pageMappingId])
            // then set process definition to UNRESOLVED:
            context.sql.executeUpdate(UPDATE_PROCESS_RESOLUTION, [processDefId])
        }
    }

    private static setAllCaseOverviewToV7(MigrationContext context) {
        context.sql.eachRow(GET_TENANTS_IDS_ON_PLATFORM) { row ->
            def tenantId = row.id as long
            context.sql.executeUpdate(UPDATE_CASE_OVERVIEW_PAGE_MAPPINGS, [tenantId, tenantId])
        }
        context.sql.executeUpdate(UPDATE_CASE_OVERVIEW_FORM_MAPPINGS)
    }

    private static cleanUpAllV6FormsResources(MigrationContext context) {
        context.sql.execute(DELETE_V6_FORMS_BAR_RESOURCE)
    }

    @Override
    String getDescription() {
        return "Remove deprecated Bonita V6 forms. Migrate deprecated V6 Case Overview pages to new V7 generic page."
    }
}

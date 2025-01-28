/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.update.version.to10_3_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractTestTo10_3_0 extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    def setup() {
        dropTestTables()
        updateContext.setVersion("10.3.0")
        createTestTables()
    }

    def cleanup() {
        dropTestTables()
    }

    def createTestTables() {
        dbUnitHelper.createTables("10_3_0")
    }

    def dropTestTables() {
        dbUnitHelper.dropTables([
            "queriable_log",
            "proc_parameter",
            "arch_flownode_instance",
            "arch_process_instance",
            "connector_instance",
            "arch_connector_instance",
            "arch_document_mapping",
            "document_mapping",
            "document",
            "pdependencymapping",
            "pdependency",
            "dependencymapping",
            "dependency",
            "tenant_resource",
            "bar_resource",
            "command",
            "message_instance",
            "waiting_event",
            "event_trigger_instance",
            "contract_data",
            "arch_contract_data",
            "processsupervisor",
            "process_definition",
            "process_content",
            "processcategorymapping",
            "category",
            "arch_process_comment",
            "process_comment",
            "icon",
            "user_membership",
            "custom_usr_inf_val",
            "custom_usr_inf_def",
            "user_contactinfo",
            "user_login",
            "user_",
            "role",
            "group_",
            "job_log",
            "job_param",
            "job_desc",
            "bpm_failure",
            "arch_bpm_failure",
            "business_app_menu",
            "business_app_page",
            "business_app",
            "profilemember",
            "profile",
            "page",
            "form_mapping",
            "page_mapping",
            "arch_data_instance",
            "data_instance",
            "arch_multi_biz_data",
            "arch_ref_biz_data_inst",
            "multi_biz_data",
            "ref_biz_data_inst",
            "pending_mapping",
            "flownode_instance",
            "process_instance",
            "tenant",
            "sequence"
        ] as String[])
    }
}

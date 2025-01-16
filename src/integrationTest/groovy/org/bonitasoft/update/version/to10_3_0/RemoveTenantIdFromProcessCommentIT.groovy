/**
 * Copyright (C) 2025 Bonitasoft S.A.
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

class RemoveTenantIdFromProcessCommentIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromProcessComment updateStep = new RemoveTenantIdFromProcessComment()

    def "should remove tenantId from process_comment and arch_process_comment tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("process_comment", "tenantId")
            hasPrimaryKeyOnTable("process_comment", "pk_process_comment")
            !hasForeignKeyOnTable("process_comment", "fk_process_comment_tenantId")

            !hasColumnOnTable("arch_process_comment", "tenantId")
            hasPrimaryKeyOnTable("arch_process_comment", "pk_arch_process_comment")
            !hasForeignKeyOnTable("arch_process_comment", "fk_arch_process_comment_tenantId")
            !hasForeignKeyOnTable("process_comment", "fk_AProcCom_tenId") // ORACLE
        }
    }
}

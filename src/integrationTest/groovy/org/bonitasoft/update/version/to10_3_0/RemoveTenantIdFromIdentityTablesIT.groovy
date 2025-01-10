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

class RemoveTenantIdFromIdentityTablesIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromIdentityTables updateStep = new RemoveTenantIdFromIdentityTables()

    def "should remove tenantId from identity tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("group_", "tenantId")
            hasPrimaryKeyOnTable("group_", "pk_group")
            !hasForeignKeyOnTable("group_", "fk_group__tenantId")

            !hasColumnOnTable("role", "tenantId")
            hasPrimaryKeyOnTable("role", "pk_role")
            !hasForeignKeyOnTable("role", "fk_role_tenantId")
            !hasUniqueKeyOnTableWithColumns("role", "tenantId", "name")
            hasUniqueKeyOnTableWithNameAndColumns("role", "uk_role_name", "name")
            !hasIndexOnTable("role", "idx_role_name")

            !hasColumnOnTable("user_", "tenantId")
            hasPrimaryKeyOnTable("user_", "pk_user")
            !hasForeignKeyOnTable("user_", "fk_user__tenantId")
            !hasUniqueKeyOnTableWithColumns("user_", "tenantId", "userName")
            hasUniqueKeyOnTableWithNameAndColumns("user_", "uk_user_username", "userName")
            !hasIndexOnTable("user_", "idx_user_name")

            !hasColumnOnTable("user_login", "tenantId")
            hasPrimaryKeyOnTable("user_login", "pk_user_login")

            !hasColumnOnTable("user_contactinfo", "tenantId")
            hasPrimaryKeyOnTable("user_contactinfo", "pk_user_contactinfo")
            !hasUniqueKeyOnTableWithColumns("user_contactinfo", "tenantId", "userId", "personal")
            hasUniqueKeyOnTableWithNameAndColumns("user_contactinfo", "uk_user_contactinfo_userid_personal", "userId", "personal")
            !hasIndexOnTable("user_contactinfo", "idx_user_contactinfo")
            !hasForeignKeyOnTable("user_contactinfo", "fk_contact_user")
            hasForeignKeyOnTable("user_contactinfo", "fk_user_contactinfo_userid")

            !hasColumnOnTable("custom_usr_inf_def", "tenantId")
            hasPrimaryKeyOnTable("custom_usr_inf_def", "pk_custom_usr_inf_def")
            !hasForeignKeyOnTable("custom_usr_inf_def", "fk_custom_usr_inf_def_tenantId")
            !hasUniqueKeyOnTableWithColumns("custom_usr_inf_def", "tenantId", "name")
            hasUniqueKeyOnTableWithNameAndColumns("custom_usr_inf_def", "uk_custom_usr_inf_def_name", "name")
            !hasIndexOnTable("custom_usr_inf_def", "idx_custom_usr_inf_def_name")

            !hasColumnOnTable("custom_usr_inf_val", "tenantId")
            hasPrimaryKeyOnTable("custom_usr_inf_val", "pk_custom_usr_inf_val")
            !hasForeignKeyOnTable("custom_usr_inf_val", "fk_custom_usr_inf_val_tenantId")
            !hasUniqueKeyOnTableWithColumns("custom_usr_inf_val", "tenantId", "definitionId", "userId")
            hasUniqueKeyOnTableWithNameAndColumns("custom_usr_inf_val", "uk_custom_usr_inf_val_definitionid_userid", "definitionId", "userId")
            !hasForeignKeyOnTable("custom_usr_inf_val", "fk_user_id")
            hasForeignKeyOnTable("custom_usr_inf_val", "fk_custom_usr_inf_val_userid")
            !hasForeignKeyOnTable("custom_usr_inf_val", "fk_definition_id")
            hasForeignKeyOnTable("custom_usr_inf_val", "fk_custom_usr_inf_val_definitionid")

            !hasColumnOnTable("user_membership", "tenantId")
            hasPrimaryKeyOnTable("user_membership", "pk_user_membership")
            !hasForeignKeyOnTable("user_membership", "fk_user_membership_tenantId")
            !hasUniqueKeyOnTableWithColumns("user_membership", "tenantId", "userId", "roleId", "groupId")
            hasUniqueKeyOnTableWithNameAndColumns("user_membership", "uk_user_membership_userid_roleid_groupid", "userId", "roleId", "groupId")

            !hasColumnOnTable("icon", "tenantId")
            hasPrimaryKeyOnTable("icon", "pk_icon")
        }
    }
}

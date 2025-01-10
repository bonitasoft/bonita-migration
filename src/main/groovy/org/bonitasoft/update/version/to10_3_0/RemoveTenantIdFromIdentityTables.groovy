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

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Remove tenantId from identity tables: group_, role, user_, user_login, user_contactinfo, custom_usr_inf_def,
 * custom_usr_inf_val, user_membership, icon
 */
class RemoveTenantIdFromIdentityTables extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            dropForeignKey("group_", "fk_group__tenantId")
            dropForeignKey("role", "fk_role_tenantId")
            dropForeignKey("user_", "fk_user__tenantId")
            dropForeignKey("user_membership", "fk_user_membership_tenantId")
            dropForeignKey("custom_usr_inf_def", "fk_custom_usr_inf_def_tenantId")
            dropForeignKey("custom_usr_inf_val", "fk_custom_usr_inf_val_tenantId")
            dropForeignKey("custom_usr_inf_val", "fk_user_id")
            dropForeignKey("custom_usr_inf_val", "fk_definition_id")
            dropForeignKey("user_contactinfo", "fk_contact_user")

            // drop indexes that are no longer needed (because they match the same columns as a unique or primary key):
            dropIndexIfExists("role", "idx_role_name")
            dropIndexIfExists("user_", "idx_user_name")
            dropIndexIfExists("user_contactinfo", "idx_user_contactinfo")
            dropIndexIfExists("custom_usr_inf_def", "idx_custom_usr_inf_def_name")

            // recreate PK:
            dropPrimaryKey("group_")
            createPrimaryKeyWithName("group_", "pk_group", "id")
            dropPrimaryKey("role")
            createPrimaryKey("role", "id")
            dropPrimaryKey("user_")
            createPrimaryKeyWithName("user_", "pk_user", "id")
            dropPrimaryKey("user_login")
            createPrimaryKey("user_login", "id")
            dropPrimaryKey("user_contactinfo")
            createPrimaryKey("user_contactinfo", "id")
            dropPrimaryKey("custom_usr_inf_def")
            createPrimaryKey("custom_usr_inf_def", "id")
            dropPrimaryKey("custom_usr_inf_val")
            createPrimaryKey("custom_usr_inf_val", "id")
            dropPrimaryKey("user_membership")
            createPrimaryKey("user_membership", "id")
            dropPrimaryKey("icon")
            createPrimaryKey("icon", "id")


            // recreate UK:
            dropUniqueKeyFromColumns("role", "tenantId", "name")
            createUniqueKey("role", "uk_role_name", "name")
            dropUniqueKeyFromColumns("user_", "tenantId", "userName")
            createUniqueKey("user_", "uk_user_username", "userName")
            dropUniqueKeyFromColumns("user_contactinfo", "tenantId", "userId", "personal")
            createUniqueKey("user_contactinfo", "uk_user_contactinfo_userid_personal", "userId", "personal")
            dropUniqueKeyFromColumns("custom_usr_inf_def", "tenantId", "name")
            createUniqueKey("custom_usr_inf_def", "uk_custom_usr_inf_def_name", "name")
            dropUniqueKeyFromColumns("custom_usr_inf_val", "tenantId", "definitionId", "userId")
            createUniqueKey("custom_usr_inf_val", "uk_custom_usr_inf_val_definitionid_userid", "definitionId", "userId")
            dropUniqueKeyFromColumns("user_membership", "tenantId", "userId", "roleId", "groupId")
            createUniqueKey("user_membership", "uk_user_membership_userid_roleid_groupid", "userId", "roleId", "groupId")

            // recreate FK:
            createForeignKey("user_contactinfo", "fk_user_contactinfo_userid", "user_", ["userId"], ["id"], true)
            createForeignKey("custom_usr_inf_val", "fk_custom_usr_inf_val_userid", "user_", ["userId"], ["id"], true)
            createForeignKey("custom_usr_inf_val", "fk_custom_usr_inf_val_definitionid", "custom_usr_inf_def", ["definitionId"], ["id"], true)

            // drop the columns:
            dropColumnIfExists("group_", "tenantId")
            dropColumnIfExists("role", "tenantId")
            dropColumnIfExists("user_", "tenantId")
            dropColumnIfExists("user_login", "tenantId")
            dropColumnIfExists("user_contactinfo", "tenantId")
            dropColumnIfExists("custom_usr_inf_def", "tenantId")
            dropColumnIfExists("custom_usr_inf_val", "tenantId")
            dropColumnIfExists("user_membership", "tenantId")
            dropColumnIfExists("icon", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from identity tables: group_, role, user_, user_login, " +
                "user_contactinfo, custom_usr_inf_def, custom_usr_inf_val, user_membership, icon"
    }
}

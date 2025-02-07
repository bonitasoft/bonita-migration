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
 * Remove tenantId from 'actor' and 'actormember' tables
 */
class RemoveTenantIdFromActorAndActorMember extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            // drop FK first:
            dropForeignKey("actor", "fk_actor_tenantId")
            dropForeignKey("actormember", "fk_actormember_tenantId")
            dropForeignKey("actormember", "fk_actormember_actorid")

            // recreate PK:
            recreatePrimaryKey("actor")
            recreatePrimaryKey("actormember")

            // recreate UK:
            dropUniqueKeyFromColumns("actor", "tenantid", "id", "scopeId", "name")
            createUniqueKey("actor", "uk_actor_id_scopeid_name", "id", "scopeId", "name")
            dropUniqueKeyFromColumns("actormember", "tenantid", "actorid", "userId", "groupId", "roleId")
            createUniqueKey("actormember", "uk_actormember_actorid_userid_groupid_roleid", "actorid", "userId", "groupId", "roleId")

            // recreate FK:
            createForeignKey("actormember", "fk_actormember_actorid", "actor", ["actorId"], ["id"], false)

            // drop the columns:
            dropColumnIfExists("actor", "tenantId")
            dropColumnIfExists("actormember", "tenantId")
        }
    }

    @Override
    String getDescription() {
        return "Remove tenantId from 'actor' and 'actormember' tables"
    }
}

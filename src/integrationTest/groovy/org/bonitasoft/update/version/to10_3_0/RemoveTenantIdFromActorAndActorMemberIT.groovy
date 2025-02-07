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

class RemoveTenantIdFromActorAndActorMemberIT extends AbstractTestTo10_3_0 {

    private RemoveTenantIdFromActorAndActorMember updateStep = new RemoveTenantIdFromActorAndActorMember()

    def "should remove tenantId from actor and actormember tables"() {
        when:
        updateStep.execute(updateContext)

        then:
        with(updateContext.databaseHelper) {
            !hasColumnOnTable("actor", "tenantId")
            hasPrimaryKeyOnTable("actor", "pk_actor")
            !hasForeignKeyOnTable("actor", "fk_actor_tenantId")
            hasUniqueKeyOnTableWithNameAndColumns("actor", "uk_actor_id_scopeid_name", "id", "scopeId", "name")

            !hasColumnOnTable("actormember", "tenantId")
            hasPrimaryKeyOnTable("actormember", "pk_actormember")
            !hasForeignKeyOnTable("actormember", "fk_actormember_tenantId")
            hasForeignKeyOnTable("actormember", "fk_actormember_actorid")
            hasUniqueKeyOnTableWithNameAndColumns("actormember", "uk_actormember_actorid_userid_groupid_roleid", "actorid", "userId", "groupId", "roleId")
        }
    }
}

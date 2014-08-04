/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.migration.versions.v6_3_2_to_6_3_3

import groovy.sql.Sql
import org.bonitasoft.migration.core.IOUtil
import org.bonitasoft.migration.core.MigrationUtil

/**
 * @author Baptiste Mesta
 */
class ResetFailedGateways {

    public migrate(String dbVendor, Sql sql) {

        def tenantsId = MigrationUtil.getTenantsId(dbVendor, sql)

        println "Executing update for each tenant : " + tenantsId
        IOUtil.executeWrappedWithTabs {
            tenantsId.each {
                println "For tenant with id = " + it
                def tenantId = it;
                "DELETE FROM arch_data_mapping WHERE NOT EXISTS (SELECT NULL FROM arch_data_instance i WHERE i.sourceObjectId = arch_data_mapping.dataInstanceId)"

                //delete arch gateways for completed gateways (it does not delete the arch gateway in executing)
                sql.execute("DELETE FROM arch_flownode_instance " +
                        "WHERE tenantId = $tenantId " +
                        "AND (stateName = 'completed' OR stateName = 'failed' )" +
                        "AND sourceObjectId IN " +
                        "(SELECT flownode_instance.id FROM flownode_instance WHERE " +
                        "flownode_instance.tenantId = $tenantId " +
                        "AND flownode_instance.stateName = 'failed' " +
                        "AND flownode_instance.hitBys LIKE 'FINISH:%')")
                //delete arch gateways for executing gateways
                sql.execute("DELETE FROM arch_flownode_instance " +
                        "WHERE tenantId = $tenantId " +
                        "AND sourceObjectId IN " +
                        "(SELECT flownode_instance.id FROM flownode_instance WHERE " +
                        "flownode_instance.tenantId = $tenantId " +
                        "AND (flownode_instance.stateName = 'failed' OR flownode_instance.stateName = 'completed')" +
                        "AND flownode_instance.hitBys NOT LIKE 'FINISH:%')")

                //update gateways that are in executing
                println "Updated " + sql.executeUpdate('''
UPDATE flownode_instance
SET
    terminal = ?,
    stable = ?,
    stateId = ?,
    stateName = ?,
    prev_state_id = ?
WHERE stateName = 'failed'
    AND hitBys LIKE 'FINISH:%'
    AND tenantId = ?
''',
                        true, true, 2, "completed", 61, tenantId) + " gateway in executing state"
                //update gateways that are in completing
                println "Updated " + sql.executeUpdate('''
UPDATE flownode_instance
SET
    terminal = ?,
    stable = ?,
    stateId = ?,
    stateName = ?,
    prev_state_id = ?
WHERE (stateName = 'failed' OR stateName = 'completed')
    AND hitBys NOT LIKE 'FINISH:%'
    AND tenantId = ?
''',
                        false, false, 61, "executing", 0,  tenantId) + " gateway in completed state"
            }
        }
    }

}

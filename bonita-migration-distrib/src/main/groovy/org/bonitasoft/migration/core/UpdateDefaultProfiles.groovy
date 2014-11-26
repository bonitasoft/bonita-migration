/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.bonitasoft.migration.core

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
class UpdateDefaultProfiles extends DatabaseMigrationStep {


    UpdateDefaultProfiles(Sql sql, String dbVendor) {
        super(sql,dbVendor)
    }

    public migrate(){
        def tenantsId = MigrationUtil.getTenantsId(dbVendor, sql)
        IOUtil.executeWrappedWithTabs {
            tenantsId.each { long tenantId ->
                def adminProfileId = selectFirstRow("SELECT id FROM profile WHERE name = 'Administrator' AND tenantId = $tenantId").id
                def configProfileEntryId = selectFirstRow("SELECT id FROM profileentry WHERE name = 'Configuration' AND tenantId = $tenantId").id
                if(selectFirstRow("SELECT count(id) FROM profileentry WHERE page='thememoredetailsadminext' AND tenantId = $tenantId").getAt(0) == 0){
                    def id = getAndUpdateNextSequenceId(9991, tenantId);
                    println "insert new profile entry $id for theme management"
                    executeUpdate("INSERT INTO profileentry (tenantId, id, profileId, name, description, parentId, index_, type, page) " +
                            "VALUES ($tenantId, $id, $adminProfileId,'Look & Feel', 'LooknFeel', $configProfileEntryId, 2, 'link', 'thememoredetailsadminext')")
                }else{
                    println "theme management profile entry was already here"
                }
            }
        }
    }

}

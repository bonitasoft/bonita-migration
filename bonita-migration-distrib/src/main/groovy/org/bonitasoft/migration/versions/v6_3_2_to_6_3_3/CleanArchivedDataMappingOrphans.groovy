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

/**
 * @author Matthieu Chaffotte
 *
 */
class CleanArchivedDataMappingOrphans {

    public migrate(Sql sql) {
    	println "Deleting archived data mapping orphans for all tenants";
		sql.execute("DELETE FROM arch_data_mapping WHERE NOT EXISTS (SELECT NULL FROM arch_data_instance i WHERE i.sourceObjectId = arch_data_mapping.dataInstanceId)");
        println sql.updateCount + " archived data mapping orphans have been deleted";
    }

}

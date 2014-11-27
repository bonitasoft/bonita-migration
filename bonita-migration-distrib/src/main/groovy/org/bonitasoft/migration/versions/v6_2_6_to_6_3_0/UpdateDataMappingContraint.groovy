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
package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0

import groovy.sql.Sql

import org.bonitasoft.migration.core.MigrationUtil



/**
 * @author Elias Ricken de Medeiros
 *
 */
class UpdateDataMappingContraint {

    private Sql sql
    private String dbvendor
    private feature

    def upgradeNamedConstraint = [
        { return hasNamedConstraint() },
        {  return updateConstraint() }
    ]

    def upgradeUnNamedConstraint = [
        {  return hasConstraintCardinality() },
        {  return updateConstraint() }
    ]

    def scripts = [
        "mysql": upgradeNamedConstraint,
        "postgres": upgradeNamedConstraint,
        "oracle": upgradeUnNamedConstraint,
        "sqlserver": upgradeUnNamedConstraint
    ];

    public migrate () {
        boolean migrated = true;
        for (request in scripts[dbvendor]) {
            boolean mustContinue = request()
            if(!mustContinue) {
                println "The constraint is already up to date, nothing to do"
                migrated = false;
                break;
            }
        }
        return migrated;
    }

    private boolean hasNamedConstraint() {
        def firstRow = executeCheckConstraintRequest()
        return firstRow != null
    }

    private boolean hasConstraintCardinality() {
        def firstRow = executeCheckConstraintRequest()
        if(firstRow.nbConstraint == 1) {
            return firstRow.nbColumn == 3
        }
        return true;
    }

    private groovy.sql.GroovyRowResult executeCheckConstraintRequest() {
        def checkConstraintFile = new File(feature.getPath() + File.separator + "${dbvendor}-check-constraint.sql");
        def firstRow = sql.firstRow(checkConstraintFile.text)
        return firstRow
    }

    private boolean updateConstraint() {
        println "Updating constraint from 'UNIQUE (containerId, containerType, dataName) to 'UNIQUE (tenantId, containerId, containerType, dataName)'"
        try {
            MigrationUtil.executeDefaultSqlFile(feature, dbvendor, sql)
        }catch(Throwable t){
            println "Unable to update the constraint, try to append  \"AND all_cons.OWNER = '<name of the user>'\" to the file in versions/6.2.6-6.3.0/Database/008_BS-296/oracle-check-constraint.sql"
            throw t
        }
        return true
    }
}

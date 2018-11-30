/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.migration.version.to7_1_5

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification
/**
 * @author Laurent Leseigneur
 */
class UpdateNullReachStateDateIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.1.5")
    }

    def cleanup() {
        dbUnitHelper.dropTables(["flownode_instance", "arch_flownode_instance"] as String[])
    }

    def "should flow node tables do not have null values in arch_flownode and flownode tables"() {
        setup:
        dbUnitHelper.createTables("7_1_5", "flownodeTables")

        migrationContext.sql.executeInsert("INSERT INTO flownode_instance (tenantId,id,name,kind,stateId,terminal,stateName,stable,flownodedefinitionid,rootcontainerid,parentcontainerid,prev_state_id,statecategory,logicalgroup1,logicalgroup2,logicalgroup4,tokenCount) VALUES (${1},${200},${'step1'},${'subProc'},${31},${dbUnitHelper.falseValue()},${'executing'},${dbUnitHelper.falseValue()},${12345},${16543},${16543},${0},${'NORMAL'},${1},${2},${4},${1})")
        migrationContext.sql.executeInsert("INSERT INTO flownode_instance (tenantId,id,name,kind,stateId,terminal,stateName,stable,flownodedefinitionid,rootcontainerid,parentcontainerid,prev_state_id,statecategory,logicalgroup1,logicalgroup2,logicalgroup4,tokenCount,reachedStateDate,lastUpdateDate) VALUES (${1},${201},${'step1'},${'subProc'},${31},${dbUnitHelper.falseValue()},${'executing'},${dbUnitHelper.falseValue()},${12345},${16543},${16543},${0},${'NORMAL'},${1},${2},${4},${1},${123},${456})")

        migrationContext.sql.executeInsert("INSERT INTO arch_flownode_instance (tenantId,id,name,kind,stateId,terminal,stateName,stable,flownodedefinitionid,rootcontainerid,parentcontainerid,logicalgroup1,logicalgroup2,logicalgroup4,archivedate,aborting) VALUES (${1},${200},${'step1'},${'subProc'},${31},${dbUnitHelper.falseValue()},${'executing'},${dbUnitHelper.falseValue()},${12345},${16543},${16543},${1},${2},${4},${45353276},${dbUnitHelper.falseValue()})")
        migrationContext.sql.executeInsert("INSERT INTO arch_flownode_instance (tenantId,id,name,kind,stateId,terminal,stateName,stable,flownodedefinitionid,rootcontainerid,parentcontainerid,logicalgroup1,logicalgroup2,logicalgroup4,archivedate,aborting,reachedStateDate,lastUpdateDate) VALUES (${1},${201},${'step1'},${'subProc'},${31},${dbUnitHelper.falseValue()},${'executing'},${dbUnitHelper.falseValue()},${12345},${16543},${16543},${1},${2},${4},${45353276},${dbUnitHelper.falseValue()},${123},${456})")

        when:
        new UpdateNullReachStateDate().execute(migrationContext)

        then:
        def List flowNodes = migrationContext.sql.rows("SELECT * FROM flownode_instance ORDER BY id")
        flowNodes.get(0).reachedStateDate == 0
        flowNodes.get(0).lastUpdateDate == 0
        flowNodes.get(1).reachedStateDate == 123
        flowNodes.get(1).lastUpdateDate == 456
        def List archFlowNodes = migrationContext.sql.rows("SELECT * FROM arch_flownode_instance ORDER BY id")
        archFlowNodes.get(0).reachedStateDate == 0
        archFlowNodes.get(0).lastUpdateDate == 0
        archFlowNodes.get(1).reachedStateDate == 123
        archFlowNodes.get(1).lastUpdateDate == 456


    }

}

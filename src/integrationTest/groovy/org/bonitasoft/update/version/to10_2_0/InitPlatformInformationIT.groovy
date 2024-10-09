/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to10_2_0

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep
import org.junit.jupiter.api.Assumptions
import spock.lang.Shared
import spock.lang.Specification

class InitPlatformInformationIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private InitPlatformInformation updateStep = new InitPlatformInformation()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("10_2_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["platform", "arch_process_instance"] as String[])
    }


    def "should add counter in `information` column on table `platform`"() {
        Assumptions.assumeTrue(updateContext.dbVendor ==  UpdateStep.DBVendor.POSTGRES, "Only PostgreSQL is supported for this update")
        def startDate1 = System.currentTimeMillis() - 222222L
        def startDate2 = System.currentTimeMillis() - 111111L

        given:
        updateContext.sql.executeInsert("""insert into platform(id, version, initial_bonita_version, application_version,
maintenance_message_active, created, created_by, information) values (?,?,?,?,?,?,?,?)""",
                1L, "10.1", "10.1.0", "0.0.0", false, 112133L, "platformAdmin", null)

        updateContext.sql.executeInsert("""insert into arch_process_instance(tenantid, id, name, processDefinitionId, startDate, startedBy,
startedBySubstitute, endDate, archiveDate, stateId, lastUpdate, sourceObjectId) values (?,?,?,?,?,?,?,?,?,?,?,?)""",
                0L, 1, "process1", 123456L, startDate1, -1, -1, 0L, System.currentTimeMillis(), 0, System.currentTimeMillis(), 123L)
        updateContext.sql.executeInsert("""insert into arch_process_instance(tenantid, id, name, processDefinitionId, startDate, startedBy,
startedBySubstitute, endDate, archiveDate, stateId, lastUpdate, sourceObjectId) values (?,?,?,?,?,?,?,?,?,?,?,?)""",
                0L, 2, "process1", 548598345L, startDate2, -1, -1, 0L, System.currentTimeMillis(), 0, System.currentTimeMillis(), 12431L)

        when:
        updateStep.execute(updateContext)

        then:
        def information = updateContext.sql.firstRow("select information from platform").information as String
        def dates = new ObjectMapper().readValue(SimpleEncryptor.decrypt(information), new TypeReference<List<Long>>() {})
        dates[0] == startDate1
        dates[1] == startDate2
    }
}

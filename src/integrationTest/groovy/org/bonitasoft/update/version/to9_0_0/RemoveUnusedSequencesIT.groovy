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
package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.DBUnitHelper
import org.bonitasoft.update.core.UpdateContext
import spock.lang.Shared
import spock.lang.Specification

class RemoveUnusedSequencesIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    UpdateContext updateContext = dbUnitHelper.context

    private RemoveUnusedSequences updateStep = new RemoveUnusedSequences()

    def setup() {
        dropTestTables()
        dbUnitHelper.createTables("9_0_0")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["temporary_content", "sequence", "platform", "ref_biz_data_inst", "process_instance", "tenant"] as String[])
    }


    def "should remove useless sequences"() {
        given:
        updateContext.sql.executeInsert("""INSERT INTO sequence (tenantid, id, nextid) VALUES (1,10120,54)""")
        updateContext.sql.executeInsert("""INSERT INTO sequence (tenantid, id, nextid) VALUES (-1,30,1111)""")
        updateContext.sql.executeInsert("""INSERT INTO sequence (tenantid, id, nextid) VALUES (-1,31,2222)""")
        updateContext.sql.executeInsert("""INSERT INTO sequence (tenantid, id, nextid) VALUES (1,30,1234)""")
        updateContext.sql.executeInsert("""INSERT INTO sequence (tenantid, id, nextid) VALUES (1,31,5678)""")

        when:
        updateStep.execute(updateContext)

        then:
        // validate sequence deletions:
        updateContext.databaseHelper.getSequenceValue(-1, 30) == null
        updateContext.databaseHelper.getSequenceValue(-1, 31) == null
        // not deleted:
        updateContext.databaseHelper.getSequenceValue(1, 10120)['nextid'] == 54L
        updateContext.databaseHelper.getSequenceValue(1, 30)['nextid'] == 1234L
        updateContext.databaseHelper.getSequenceValue(1, 31)['nextid'] == 5678
    }
}

/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_2_1

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Laurent Leseigneur
 */
class ContractInputSequenceIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.2.1")
        dropTestTables()
        dbUnitHelper.createTables("7_2_1/sequence", "sequence")
    }

    def cleanup() {
        dropTestTables()
    }

    private String[] dropTestTables() {
        dbUnitHelper.dropTables(["sequence"] as String[])
    }

    def "should update sequence value for contract input"() {
        setup:
        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(25, 10210, 16)")
        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(25, 10220, 1)")

        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(35, 10210, 13)")
        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(35, 10220, 18)")

        when:
        new ContractInputSequence().execute(migrationContext)

        then:
        def sequences = dbUnitHelper.context.sql.rows("SELECT tenantid, id, nextid FROM sequence s WHERE s.tenantid IN(25,35) AND s.id IN (10210,10220) ORDER BY s.tenantid, s.id ")

        sequences.size() == 2

        sequences[0].tenantid == 25
        sequences[0].id == 10210
        sequences[0].nextid == 17

        sequences[1].tenantid == 35
        sequences[1].id == 10210
        sequences[1].nextid == 19
    }

    def "should update sequence value for archived contract input"() {
        setup:
        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(12, 20210, 46)")
        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(12, 20220, 79)")

        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(16, 20210, 39)")
        dbUnitHelper.context.sql.execute("INSERT INTO sequence (tenantid,id,nextid) VALUES(16, 20220, 23)")

        when:
        new ContractInputSequence().execute(migrationContext)

        then:
        def sequences = dbUnitHelper.context.sql.rows("SELECT tenantid, id, nextid FROM sequence s WHERE s.tenantid IN (12,16) AND s.id IN (20210,20220) ORDER BY s.tenantid, s.id ")

        sequences.size() == 2

        sequences[0].tenantid == 12
        sequences[0].id == 20210
        sequences[0].nextid == 80

        sequences[1].tenantid == 16
        sequences[1].id == 20210
        sequences[1].nextid == 40

    }

}

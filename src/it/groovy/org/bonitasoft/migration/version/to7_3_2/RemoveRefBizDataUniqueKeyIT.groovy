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
package org.bonitasoft.migration.version.to7_3_2

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat
import static org.bonitasoft.migration.core.MigrationStep.DBVendor.ORACLE

/**
 * @author Laurent Leseigneur
 */
class RemoveRefBizDataUniqueKeyIT extends Specification {

    @Shared
    DBUnitHelper dbUnitHelper = DBUnitHelper.getInstance()
    @Shared
    MigrationContext migrationContext = dbUnitHelper.context

    def setup() {
        migrationContext.setVersion("7.3.2")
        dropTables()
        dbUnitHelper.createTables("7_3_2/bizdata")
    }

    def cleanup() {
        dropTables()
    }

    private String[] dropTables() {
        dbUnitHelper.dropTables(["ref_biz_data_inst", "arch_ref_biz_data_inst"] as String[])
    }

    def "should remove unique key"() {
        given:
        def removeRefBizDataUniqueKey = new RemoveRefBizDataUniqueKey()
        def refBizDataInstPkName = removeRefBizDataUniqueKey.getUkName(migrationContext.dbVendor)

        assertThat(dbUnitHelper.hasUniqueKeyOnTable("ref_biz_data_inst", refBizDataInstPkName)).isTrue()
        assertThat(dbUnitHelper.hasUniqueKeyOnTable("arch_ref_biz_data_inst", "uk_arch_ref_biz_data_inst")).isTrue()
        if (ORACLE == migrationContext.dbVendor) {
            assertThat(dbUnitHelper.hasIndexOnTable("ref_biz_data_inst", refBizDataInstPkName)).isTrue()
            assertThat(dbUnitHelper.hasIndexOnTable("arch_ref_biz_data_inst", "uk_arch_ref_biz_data_inst")).isTrue()
        }

        when:
        removeRefBizDataUniqueKey.execute(migrationContext)

        then:
        assertThat(dbUnitHelper.hasUniqueKeyOnTable("ref_biz_data_inst", refBizDataInstPkName)).isFalse()
        assertThat(dbUnitHelper.hasUniqueKeyOnTable("arch_ref_biz_data_inst", "uk_arch_ref_biz_data_inst")).isFalse()

        assertThat(dbUnitHelper.hasIndexOnTable("ref_biz_data_inst", "uk_ref_biz_data_inst")).isFalse()
        assertThat(dbUnitHelper.hasIndexOnTable("arch_ref_biz_data_inst", "uk_arch_ref_biz_data_inst")).isFalse()

        migrationContext.sql.executeInsert("insert into ref_biz_data_inst(name, proc_inst_id, fn_inst_id, tenantid,id,kind,data_classname ) values (?,?,?,?,?,?,?)",
                "name", 12L, 14L, 2L, 1L, "kind", "dataClassName") != null
        migrationContext.sql.executeInsert("insert into ref_biz_data_inst(name, proc_inst_id, fn_inst_id, tenantid,id,kind,data_classname ) values (?,?,?,?,?,?,?)",
                "name", 12L, 14L, 2L, 2L, "kind", "dataClassName") != null

        migrationContext.sql.executeInsert("insert into arch_ref_biz_data_inst(name, orig_proc_inst_id, orig_fn_inst_id, tenantid,id,kind,data_classname ) values (?,?,?,?,?,?,?)",
                "name", 12L, 14L, 2L, 3L, "kind", "dataClassName") != null
        migrationContext.sql.executeInsert("insert into arch_ref_biz_data_inst(name, orig_proc_inst_id, orig_fn_inst_id, tenantid,id,kind,data_classname ) values (?,?,?,?,?,?,?)",
                "name", 12L, 14L, 2L, 4L, "kind", "dataClassName") != null

    }


}

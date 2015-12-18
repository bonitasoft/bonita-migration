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
package org.bonitasoft.migration.version.to7_2_0

import org.bonitasoft.migration.DBUnitHelper
import org.bonitasoft.migration.core.Logger
import org.bonitasoft.migration.core.MigrationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Emmanuel Duchastenier
 */
class EnsureAddedTablesForArchBusinessData extends Specification {

    @Shared
    Logger logger = Mock(Logger)

    @Shared
    MigrationContext migrationContext = new MigrationContext(logger: logger)

    @Shared
    DBUnitHelper dbUnitHelper = new DBUnitHelper(migrationContext)

    def setup() {
        migrationContext.setVersion("7.2.0")
    }

    def cleanup() {
        dbUnitHelper.dropTables(["arch_multi_biz_data", "arch_ref_biz_data_inst"] as String[])
    }

    def "should 7.2.0 platform create table arch_ref_biz_data_inst"() {
        when:
        new AddArchRefBusinessDataTables().execute(migrationContext)

        then:
        migrationContext.sql.execute("SELECT tenantid, id, kind, name, orig_proc_inst_id, orig_fn_inst_id, data_id, data_classname FROM arch_ref_biz_data_inst")
        // there should be no error here
    }

    def "should 7.2.0 platform create table arch_multi_biz_data"() {
        when:
        new AddArchRefBusinessDataTables().execute(migrationContext)

        then:
        migrationContext.sql.execute("SELECT tenantid, id, idx, data_id FROM arch_multi_biz_data")
        // there should be no error here
    }

    def "should 7.2.0 platform add unique / primary / foreign keys and index on table arch_ref_biz_data_inst"() {
        when:
        new AddArchRefBusinessDataTables().execute(migrationContext)

        then:
        def columnDefinitions1 = dbUnitHelper.getIndexDefinition("arch_ref_biz_data_inst", "idx_arch_biz_data_inst1").getColumnDefinitions()
        columnDefinitions1.size() == 2
        columnDefinitions1.get(0).columnName == "tenantid"
        columnDefinitions1.get(0).position == 1
        columnDefinitions1.get(1).columnName == "orig_proc_inst_id"
        columnDefinitions1.get(1).position == 2

        def columnDefinitions2 = dbUnitHelper.getIndexDefinition("arch_ref_biz_data_inst", "idx_arch_biz_data_inst2").getColumnDefinitions()
        columnDefinitions2.size() == 2
        columnDefinitions2.get(0).columnName == "tenantid"
        columnDefinitions2.get(0).position == 1
        columnDefinitions2.get(1).columnName == "orig_fn_inst_id"
        columnDefinitions2.get(1).position == 2

        // Can we use getIndexDefinition() method to retrieve info on PKs, FKs and UKs ? (it works at least for Postgres)
//        def columnDefinitions3 = dbUnitHelper.getIndexDefinition("arch_ref_biz_data_inst", "pk_arch_ref_biz_data_inst").getColumnDefinitions()
//        columnDefinitions3.size() == 2
//        columnDefinitions3.get(0).columnName == "tenantid"
//        columnDefinitions3.get(0).position == 1
//        columnDefinitions3.get(1).columnName == "id"
//        columnDefinitions3.get(1).position == 2
//
//        def columnDefinitions4 = dbUnitHelper.getIndexDefinition("arch_ref_biz_data_inst", "uk_arch_ref_biz_data_inst").getColumnDefinitions()
//        columnDefinitions4.size() == 4
//        columnDefinitions4.get(0).columnName == "name"
//        columnDefinitions4.get(0).position == 1
//        columnDefinitions4.get(1).columnName == "orig_proc_inst_id"
//        columnDefinitions4.get(1).position == 2
//        columnDefinitions4.get(2).columnName == "orig_fn_inst_id"
//        columnDefinitions4.get(2).position == 3
//        columnDefinitions4.get(3).columnName == "tenantid"
//        columnDefinitions4.get(3).position == 4
    }

    def "should 7.2.0 platform add 1 primary key and 1 foreign key on table arch_multi_biz_data"() {
        when:
        new AddArchRefBusinessDataTables().execute(migrationContext)

        then:
        // Can we use getIndexDefinition() method to retrieve info on PKs, FKs and UKs ? (it works at least for Postgres), if yes, uncomment this test :
//        def columnDefinitions1 = dbUnitHelper.getIndexDefinition("arch_multi_biz_data", "pk_arch_rbdi_mbd").getColumnDefinitions()
//        columnDefinitions1.size() == 3
//        columnDefinitions1.get(0).columnName == "tenantid"
//        columnDefinitions1.get(0).position == 1
//        columnDefinitions1.get(1).columnName == "id"
//        columnDefinitions1.get(1).position == 2
//        columnDefinitions1.get(2).columnName == "data_id"
//        columnDefinitions1.get(2).position == 3

        dbUnitHelper.hasForeignKeyOnTable("arch_multi_biz_data", "fk_arch_rbdi_mbd")
    }

}

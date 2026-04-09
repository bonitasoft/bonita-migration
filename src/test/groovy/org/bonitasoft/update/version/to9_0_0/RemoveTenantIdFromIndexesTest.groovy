/**
 * Copyright (C) 2026 Bonitasoft S.A.
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


import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.database.DatabaseHelper
import org.bonitasoft.update.core.database.schema.IndexDefinition
import spock.lang.Specification

import static com.github.zafarkhaja.semver.Version.parse

class RemoveTenantIdFromIndexesTest extends Specification {

    def dbHelper = Mock(DatabaseHelper)
    def logger = Mock(Logger)
    def step = new RemoveTenantIdFromIndexes()
    def targetVersion = parse("9.0.0")

    def setup() {
        step.logger = logger
    }

    def "should skip when target index already exists with correct name"() {
        given:
        def target = new IndexDefinition("ref_biz_data_inst", "pk_ref_biz_data_inst", true, "id")
        dbHelper.getIndexDefinition("ref_biz_data_inst", true, "id") >> target

        when:
        step.dealWithTargetIndex(target, dbHelper, targetVersion)

        then:
        0 * dbHelper.renameIndex(_, _, _)
        0 * dbHelper.dropIndexIfExists(_, _)
        0 * dbHelper.createIndex(_, _, _, _)
    }

    def "should rename index when target name is not taken"() {
        given:
        def target = new IndexDefinition("ref_biz_data_inst", "pk_ref_biz_data_inst", true, "id")
        def foundIndex = new IndexDefinition("ref_biz_data_inst", "old_index_name", true, "id")
        dbHelper.getIndexDefinition("ref_biz_data_inst", true, "id") >> foundIndex
        dbHelper.hasIndexOnTable("ref_biz_data_inst", "pk_ref_biz_data_inst") >> false

        when:
        step.dealWithTargetIndex(target, dbHelper, targetVersion)

        then:
        1 * dbHelper.renameIndex("ref_biz_data_inst", "old_index_name", "pk_ref_biz_data_inst")
        0 * dbHelper.dropIndexIfExists(_, _)
    }

    def "should drop old index and rename good one when target name already exists"() {
        given:
        def target = new IndexDefinition("ref_biz_data_inst", "pk_ref_biz_data_inst", true, "id")
        def foundIndex = new IndexDefinition("ref_biz_data_inst", "wrong_name_ref_biz_data_inst", true, "id")
        dbHelper.getIndexDefinition("ref_biz_data_inst", true, "id") >> foundIndex
        dbHelper.hasIndexOnTable("ref_biz_data_inst", "pk_ref_biz_data_inst") >> true

        when:
        step.dealWithTargetIndex(target, dbHelper, targetVersion)

        then:
        1 * dbHelper.dropIndexIfExists("ref_biz_data_inst", "pk_ref_biz_data_inst")
        1 * dbHelper.renameIndex("ref_biz_data_inst", "wrong_name_ref_biz_data_inst", "pk_ref_biz_data_inst")
    }

    def "should create index when target does not exist"() {
        given:
        def target = new IndexDefinition("ref_biz_data_inst", "pk_ref_biz_data_inst", true, "id")
        dbHelper.getIndexDefinition("ref_biz_data_inst", true, "id") >> null

        when:
        step.dealWithTargetIndex(target, dbHelper, targetVersion)

        then:
        1 * dbHelper.createIndex("ref_biz_data_inst", "pk_ref_biz_data_inst", true, "id")
    }
}

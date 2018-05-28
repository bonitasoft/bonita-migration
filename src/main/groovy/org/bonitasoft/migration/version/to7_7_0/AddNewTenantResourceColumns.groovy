/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_7_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class AddNewTenantResourceColumns extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def databaseHelper = context.databaseHelper
        def dbVendorColumns = DbVendorColumns.valueOf(context.dbVendor.name())
        databaseHelper.addColumnIfNotExist("tenant_resource", "lastUpdatedBy", dbVendorColumns.getType("lastUpdatedBy"), "-1", "NOT NULL")
        databaseHelper.addColumnIfNotExist("tenant_resource", "lastUpdateDate", dbVendorColumns.getType("lastUpdateDate"), null, null)
        databaseHelper.addColumnIfNotExist("tenant_resource", "state", dbVendorColumns.getType("state"), "\'INSTALLED\'", "NOT NULL")
    }

    @Override
    String getDescription() {
        return "Add new columns to the tenant_resource table"
    }

    private static enum DbVendorColumns {
        MYSQL([lastUpdatedBy: "BIGINT", lastUpdateDate: "BIGINT", state: "VARCHAR(50)"]),
        // conversion VARCHAR => VARCHAR2 is automatically done by databaseHelper:
        ORACLE([lastUpdatedBy: "NUMBER(19, 0)", lastUpdateDate: "NUMBER(19, 0)", state: "VARCHAR(50)"]),
        POSTGRES([lastUpdatedBy: "INT8", lastUpdateDate: "INT8", state: "VARCHAR(50)"]),
        // conversion VARCHAR => NVARCHAR is automatically done by databaseHelper:
        SQLSERVER([lastUpdatedBy: "NUMERIC(19, 0)", lastUpdateDate: "NUMERIC(19, 0)", state: "VARCHAR(50)"])

        private final Map<String, String> columnTypes

        DbVendorColumns(Map<String, String> columnTypes) {
            this.columnTypes = columnTypes
        }

        def getType(String columnName) {
            columnTypes.get(columnName)
        }

    }

}

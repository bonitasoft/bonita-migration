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
package org.bonitasoft.migration;

import org.dbunit.DatabaseUnitException
import org.dbunit.assertion.DbUnitAssert
import org.dbunit.dataset.Column
import org.dbunit.dataset.ColumnFilterTable
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.filter.DefaultColumnFilter



/**
 * @author Elias Ricken de Medeiros
 *
 */
public class NamedColumnsAssertion {

    public static void assertEqualsForColumns(final IDataSet expectedDataset,
            final IDataSet actualDataset,
            final List<String> columnsToCheck) throws DatabaseUnitException  {
            List<String> lowerCaseColumns = toLowerCase(columnsToCheck);
        def dbAssert = new DbUnitAssert();
        def iterator = expectedDataset.iterator();
        while(iterator.next()) {
            def table = iterator.getTable();
            def tableName = table.getTableMetaData().getTableName();
            dbAssert.assertEquals(selectNamedColumns(table, lowerCaseColumns), selectNamedColumns(actualDataset.getTable(tableName), lowerCaseColumns))
        }
    }
            
    public static List<String> toLowerCase(List<String> columnNames) {
        List<String> lowerCaseNames = [];
        for (name in  columnNames) {
            lowerCaseNames.add(name.toLowerCase());
        }
        return lowerCaseNames;
    }
    
    public static ITable selectNamedColumns(ITable table, List<String> columnsToCheck)
            throws DataSetException {
        DefaultColumnFilter columnFilter = new DefaultColumnFilter();
        Column[] columns = table.getTableMetaData().getColumns();
        for (col in columns) {
            String columName = col.getColumnName();
            if(!columnsToCheck.contains(columName.toLowerCase())) {
                columnFilter.excludeColumn(columName);
            }
        }

        return new ColumnFilterTable(table, columnFilter);
    }
    
}

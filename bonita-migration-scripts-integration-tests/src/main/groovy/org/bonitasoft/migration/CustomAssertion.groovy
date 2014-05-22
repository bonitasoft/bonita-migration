package org.bonitasoft.migration;

import org.dbunit.DatabaseUnitException
import org.dbunit.assertion.DbUnitAssert
import org.dbunit.dataset.IDataSet

/**
 * Created by Vincent Elcrin
 * Date: 05/12/13
 * Time: 19:20
 */
public class CustomAssertion {

    public static final DbUnitAssert INSTANCE = new CustomBdUnitAssertion();

    public static void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
        INSTANCE.assertEquals(expectedDataSet, actualDataSet);
    }

    public static class CustomBdUnitAssertion extends DbUnitAssert {

        @Override
        protected boolean skipCompare(String columnName, Object expectedValue,
                Object actualValue) {
            if ("<skip>".equalsIgnoreCase(String.valueOf(expectedValue))) {
                return true;
            }
            return super.skipCompare(columnName, expectedValue, actualValue);
        }
                
    }
}
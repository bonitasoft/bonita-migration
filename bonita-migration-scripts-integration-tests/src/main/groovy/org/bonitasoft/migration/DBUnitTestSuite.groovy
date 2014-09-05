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
package org.bonitasoft.migration

import junit.framework.TestSuite

import org.bonitasoft.migration.versions.v6_2_2to_6_2_3.BoundaryTokensMigrationIT
import org.bonitasoft.migration.versions.v6_2_6_to_6_3_0.IndexExistsCheckerIT
import org.bonitasoft.migration.versions.v6_2_6_to_6_3_0.UpdateDataMappingContraintIT
import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.UpdatedDefaultCommandsIT
import org.bonitasoft.migration.versions.v6_3_2_to_6_3_3.ArchivedDataInstancesIT
import org.bonitasoft.migration.versions.v6_3_2_to_6_3_3.ResetFailedGatewaysIT
import org.bonitasoft.migration.versions.v6_3_2_to_6_4_0.CreateApplicationTablesIT
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.ChangeDocumentsStructureIT
import org.junit.runner.JUnitCore


/**
 * @author Elias Ricken de Medeiros
 *
 */
class DBUnitTestSuite extends TestSuite {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DBUnitTestSuite.class.getName());
    }

    public static TestSuite suite() throws Exception {
        TestSuite suite = new TestSuite();
        GroovyTestSuite gsuite = new GroovyTestSuite();
        suite.addTestSuite(UpdateDataMappingContraintIT.class);
        suite.addTestSuite(IndexExistsCheckerIT.class);
        suite.addTestSuite(BoundaryTokensMigrationIT.class);
        suite.addTestSuite(UpdatedDefaultCommandsIT.class);
        suite.addTestSuite(ArchivedDataInstancesIT.class);
        suite.addTestSuite(ResetFailedGatewaysIT.class);
        suite.addTestSuite(CreateApplicationTablesIT.class);
        suite.addTestSuite(ChangeDocumentsStructureIT.class);
        return suite;
    }
}

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
import org.bonitasoft.migration.versions.to_7_0_0.AddApplicationLayoutAndThemeIT
import org.bonitasoft.migration.versions.to_7_0_0.ApplicationPageRetrieverIT
import org.bonitasoft.migration.versions.to_7_0_0.ApplicationRetrieverIT
import org.bonitasoft.migration.versions.v6_2_2to_6_2_3.BoundaryTokensMigrationIT
import org.bonitasoft.migration.versions.v6_2_6_to_6_3_0.IndexExistsCheckerIT
import org.bonitasoft.migration.versions.v6_2_6_to_6_3_0.UpdateDataMappingContraintIT
import org.bonitasoft.migration.versions.v6_3_1_to_6_3_2.UpdatedDefaultCommandsIT
import org.bonitasoft.migration.versions.v6_3_2_to_6_3_3.ArchivedDataInstancesIT
import org.bonitasoft.migration.versions.v6_3_2_to_6_3_3.ResetFailedGatewaysIT
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.ChangeDocumentsStructureIT
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.CreateApplicationTablesIT
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.UpdateDefaultProfilesIT
import org.bonitasoft.migration.versions.v6_3_x_to_6_4_0.UpdateProfileEntriesIT
import org.bonitasoft.migration.versions.v6_4_0_to_6_4_1.AddArchDocumentMappingIndexIT
import org.bonitasoft.migration.versions.v6_4_0_to_6_4_1.MigrateDateDataInstancesFromWrongXMLObjectIT
import org.bonitasoft.migration.versions.v6_5_1_to_6_5_2.UpdateEventSubProcessStableFlagIT
import org.bonitasoft.migration.versions.v6_5_4_to_v7_0_0.PageTableIT
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
        //to 6.2.3
        suite.addTestSuite(BoundaryTokensMigrationIT.class);

        //to 6.3.0
        suite.addTestSuite(UpdateDataMappingContraintIT.class);
        suite.addTestSuite(IndexExistsCheckerIT.class);

        //to 6.3.2
        suite.addTestSuite(UpdatedDefaultCommandsIT.class);

        //to 6.3.3
        suite.addTestSuite(ArchivedDataInstancesIT.class);
        suite.addTestSuite(ResetFailedGatewaysIT.class);

        //to 6.4.0
        suite.addTestSuite(CreateApplicationTablesIT.class);
        suite.addTestSuite(ChangeDocumentsStructureIT.class);
        suite.addTestSuite(UpdateDefaultProfilesIT.class);
        suite.addTestSuite(UpdateProfileEntriesIT.class);

        //to 6.4.1
        suite.addTestSuite(AddArchDocumentMappingIndexIT.class);
        suite.addTestSuite(MigrateDateDataInstancesFromWrongXMLObjectIT.class);

        //to 6.5.2
        suite.addTestSuite(UpdateEventSubProcessStableFlagIT.class);

        //to 7.0.0
        suite.addTestSuite(AddApplicationLayoutAndThemeIT.class)
        suite.addTestSuite(ApplicationPageRetrieverIT.class)
        suite.addTestSuite(ApplicationRetrieverIT.class)
        suite.addTestSuite(PageTableIT.class)

        return suite
    }
}

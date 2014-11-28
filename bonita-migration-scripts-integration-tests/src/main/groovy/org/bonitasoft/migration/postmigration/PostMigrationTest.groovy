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
package org.bonitasoft.migration.postmigration

import junit.framework.TestSuite
import org.junit.runner.JUnitCore

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 *
 */
class PostMigrationTest extends TestSuite {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(PostMigrationTest.class.getName());
    }

    public static TestSuite suite() throws Exception {
        return buildTestSuite(getTestsList())
    }

    public static TestSuite buildTestSuite(List<Class> tests) {
        TestSuite suite = new TestSuite();
        for (clazz in tests) {
            def method = clazz.getMethod("runFromVersion");
            Integer fromVersion = method.invoke(null);
            if (fromVersion <= Integer.parseInt(getCurrentBonitaVersion().replaceAll("\\.", "").replace("-SNAPSHOT", ""))) {
                suite.addTestSuite(clazz);
            }
        }
        return suite
    }

    protected static List<Class> getTestsList() {
        return [];
    }

    public static String getCurrentBonitaVersion() {
        def s = File.separator;
        def File versionFile = new File(System.getProperty("bonita.home"), "server${s}platform${s}conf${s}VERSION");
        return versionFile.exists() ? versionFile.text : null;
    }
}

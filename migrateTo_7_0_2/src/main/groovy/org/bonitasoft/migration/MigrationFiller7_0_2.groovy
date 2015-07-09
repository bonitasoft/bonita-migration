/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.LocalServerTestsInitializer
import org.bonitasoft.migration.filler.FillAction
import org.bonitasoft.migration.filler.FillerInitializer
import org.bonitasoft.migration.filler.FillerShutdown

/**
 * @author Baptiste Mesta
 */
class MigrationFiller7_0_2 {


    @FillerInitializer
    public void init() {
        System.setProperty("sysprop.bonita.db.vendor", System.getProperty("dbvendor"));
        System.setProperty("db.url", System.getProperty("dburl"));
        System.setProperty("db.user", System.getProperty("dbuser"));
        System.setProperty("db.password", System.getProperty("dbpassword"));
        System.setProperty("db.database.name", "migration");
        LocalServerTestsInitializer.beforeAll();
    }


    @FillAction
    public void fillSomething() {

    }


    @FillerShutdown
    public void shutdown() {
        LocalServerTestsInitializer.getInstance().shutdown();
    }

}

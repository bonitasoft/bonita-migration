/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.migration.filler

/**
 * @author Baptiste Mesta
 */
class FillerUtils {

    /**
     * Initialize system properties to run the engine using properties from the Config.properties (migration properties)
     */
    public static void initializeEngineSystemProperties() {
        System.setProperty("sysprop.bonita.db.vendor", System.getProperty("dbvendor"));
        System.setProperty("db.url", System.getProperty("dburl"));
        System.setProperty("db.user", System.getProperty("dbuser"));
        System.setProperty("db.password", System.getProperty("dbpassword"));
        def split = System.getProperty("dburl").split("/")
        def databaseName = split[split.length - 1]
        System.setProperty("db.database.name", databaseName);
    }
}

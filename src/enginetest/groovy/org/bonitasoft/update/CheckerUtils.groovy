/**
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
 **/
package org.bonitasoft.update
/**
 * @author Baptiste Mesta
 */
class CheckerUtils {

    /**
     * Initialize system properties to run the engine using properties
     */
    static void initializeEngineSystemProperties() {
        def dbVendor = System.getProperty("db.vendor")
        System.setProperty("sysprop.bonita.db.vendor", dbVendor)
        def split = System.getProperty("db.url").split("/")
        def databaseName = split[split.length - 1]
        System.setProperty("db.database.name", databaseName)

        System.setProperty("sysprop.bonita.bdm.db.vendor", dbVendor) // same as Bonita
        System.setProperty("bdm.db.user", "business_data")
        System.setProperty("bdm.db.password", System.getProperty("bdm.db.password", System.getProperty("db.password"))) // Should be the same as Bonita for our tests²²
        System.setProperty("bdm.db.url", System.getProperty("db.url")
                .replace("/bonita", "/business_data") // postgres + mysql
                .replace("=bonita", "=business_data") // sqlserver
                ) // For all except Oracle
        if (dbVendor == 'oracle') {
            System.setProperty("bdm.db.database.name", System.getProperty("db.database.name"))
        } else {
            System.setProperty("bdm.db.database.name", "business_data")
        }
    }
}

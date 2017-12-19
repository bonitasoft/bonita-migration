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

package org.bonitasoft.migration

import groovy.sql.Sql
import org.junit.Rule
import org.junit.Test
/**
 * @author Baptiste Mesta
 */
class CheckMigratedTo7_1_2 {

    @Rule
    public Before7_2_0Initializer initializer = new Before7_2_0Initializer()

    @Test
    def void check_quartz_table_is_migrated() {

        def sql= getSql()

        // will fail with error:
        // org.postgresql.util.PSQLException: ERROR: column "long_prop_1" does not exist
        // if migration is not done as expected

        sql.execute('''
SELECT
    SCHED_NAME,
    TRIGGER_NAME,
    TRIGGER_GROUP,
    STR_PROP_1,
    STR_PROP_2,
    STR_PROP_3,
    INT_PROP_1,
    INT_PROP_2,
    LONG_PROP_1,
    LONG_PROP_2,
    DEC_PROP_1,
    DEC_PROP_2,
    BOOL_PROP_1,
    BOOL_PROP_2
FROM
    QRTZ_SIMPROP_TRIGGERS
''')
    }

    private Sql getSql() {
        def sql
        def dburl = System.getProperty("db.url");
        def dbuser = System.getProperty("db.user")
        def dbpassword = System.getProperty("db.password")
        def dbdriverClass = System.getProperty("db.driverClass")

        def db = [url: dburl, user: dbuser, password: dbpassword, driver: dbdriverClass]

        sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
        sql
    }
}
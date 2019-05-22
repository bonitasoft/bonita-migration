/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General License for more details.
 * You should have received a copy of the GNU Lesser General License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.migration.core.database

class DbConfig {
    final static String DB_URL = "db.url"
    final static String DB_USER = "db.user"
    final static String DB_PASSWORD = "db.password"
    final static String DB_DRIVER_CLASS = "db.driverClass"
    final static String DB_VENDOR = "db.vendor"
    final static String DB_POOL_SIZE_INITIAL = "db.pool.size.initial"
    final static String DB_POOL_SIZE_MAX = "db.pool.size.max"

    String dburl
    String dbDriverClassName
    String dbUser
    String dbPassword
    int dbPoolSizeInitial = 3
    int dbPoolSizeMax = 10
}

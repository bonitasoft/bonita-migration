/**
 * Copyright (C) 2018 Bonitasoft S.A.
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

package org.bonitasoft.migration.plugin.db

public class JdbcDriverDependencies {

    public final static String mysql = 'mysql:mysql-connector-java:5.1.47'
    public final static String mysql8 = 'mysql:mysql-connector-java:8.0.14'
    public final static String oracle = 'com.oracle:ojdbc:8.12.2.0.1'
    public final static String postgres = 'org.postgresql:postgresql:42.2.5'
    public final static String sqlserver = 'com.microsoft.sqlserver:mssql-jdbc:7.2.1.jre8'

}

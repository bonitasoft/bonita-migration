/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

package org.bonitasoft.migration.version.to7_0_1

import groovy.sql.Sql
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Elias Ricken de Medeiros
 */
class UpdateDefaultApplicationTheme extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        Sql sql = context.sql
        def updatedRows = 0;
        updatedRows += sql.executeUpdate("""
update page set NAME='custompage_simplextheme',
DISPLAYNAME='Simplex theme',
DESCRIPTION='Application theme based on Bootstrap "Simplex" theme. (see http://bootswatch.com/simplex/)',
CONTENTNAME='bonita-simplex-theme.zip'
where name='custompage_bootstrapdefaulttheme'
""")

        updatedRows += sql.executeUpdate("""
update page set NAME='custompage_bootstrapdefaulttheme',
DISPLAYNAME='Bootstrap default theme',
DESCRIPTION='Application theme based on bootstrap "Default" theme. (see http://bootswatch.com/default/)',
CONTENTNAME='bonita-bootstrap-default-theme.zip'
where name='custompage_defaulttheme'
""")
        println "$updatedRows application themes were update"
        return updatedRows
    }



    @Override
    String getDescription() {
       "update default application theme"
    }
}

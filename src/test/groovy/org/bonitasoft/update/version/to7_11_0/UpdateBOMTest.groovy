/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to7_11_0

import static org.bonitasoft.update.core.IOUtil.unzip
import static org.bonitasoft.update.core.IOUtil.zip

import groovy.sql.Sql
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.database.DatabaseHelper
import spock.lang.Specification

class UpdateBOMTest extends Specification {
    private Sql sql

    def 'should add namespace to BOM.xml'() {
        given:
        def updateStep = new UpdateBOM()
        def updateContext = newUpdateContext()

        when:
        def updatedClientBDMZip = updateStep.updateBOM(zip(["bom.zip": zip(["bom.xml": "<businessObjectModel></businessObjectModel>".bytes])]), updateContext, 12, 1)
        then:
        new String(unzip(unzip(updatedClientBDMZip)."bom.zip")."bom.xml") == "<businessObjectModel xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\"></businessObjectModel>"
    }

    def 'should not update when namespace is already here'() {
        given:
        def updateStep = new UpdateBOM()
        def updateContext = newUpdateContext()

        when:
        def updatedClientBDMZip = updateStep.updateBOM(zip(["bom.zip": zip(["bom.xml": "<businessObjectModel xmlns=\"http://documentation.bonitasoft.com/bdm-xml-schema/1.0\"></businessObjectModel>".bytes])]), updateContext, 12, 1)
        then:
        updatedClientBDMZip == null // it means nothing changed
    }

    def 'should not update a second time the same bom.xml'() {
        given:
        def updateStep = new UpdateBOM()
        def updateContext = newUpdateContext()

        when:
        def updatedClientBDMZip = updateStep.updateBOM(updateStep.updateBOM(zip(["bom.zip": zip(["bom.xml": "<businessObjectModel></businessObjectModel>".bytes])]), updateContext, 12, 1), updateContext, 12, 1)
        then:
        updatedClientBDMZip == null // it means nothing changed
    }


    private UpdateContext newUpdateContext() {
        def updateContext = Mock(UpdateContext)
        updateContext.databaseHelper >> Mock(DatabaseHelper)
        updateContext.logger >> Mock(Logger)
        sql = Mock(Sql)
        updateContext.sql >> sql
        updateContext
    }
}

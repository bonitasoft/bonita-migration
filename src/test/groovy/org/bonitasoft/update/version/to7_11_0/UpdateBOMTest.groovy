package org.bonitasoft.update.version.to7_11_0

import groovy.sql.Sql
import org.bonitasoft.update.core.Logger
import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.database.DatabaseHelper
import spock.lang.Specification

import static org.bonitasoft.update.core.IOUtil.unzip
import static org.bonitasoft.update.core.IOUtil.zip

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

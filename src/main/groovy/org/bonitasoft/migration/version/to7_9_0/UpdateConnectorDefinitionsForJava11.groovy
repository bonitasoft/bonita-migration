/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.migration.version.to7_9_0


import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.xml.sax.SAXParseException

import java.sql.Blob

/**
 * @author Danila Mazour
 */
class UpdateConnectorDefinitionsForJava11 extends MigrationStep {

    Map<Long, Long> dependencySequences = [:]
    Map<Long, Long> dependencyMappingSequences = [:]

    ConnectorMigrationReport connectorMigrationReport = new ConnectorMigrationReport()


    @Override
    def execute(MigrationContext context) {
        initializeSequences(context)
        def processes = getProcesses(context)
        processes.each { process ->
            migrateConnector(process, "webservice", context, "webservice", "webservice-1.2.2.impl")
            migrateConnector(process, "email", context, "email", "email-1.1.0.impl")
            migrateConnector(process, "cmis", context, "cmis",
                    "cmis-createfolder.impl",
                    "cmis-deletedocument.impl",
                    "cmis-deletefolder.impl",
                    "cmis-deleteversionofdocument.impl",
                    "cmis-downloaddocument.impl",
                    "cmis-listdocuments.impl",
                    "cmis-uploadnewdocument.impl",
                    "cmis-uploadnewdocuments.impl",
                    "cmis-uploadnewversionofdocument.impl")
        }
        updateSequenceTable(context)
        connectorMigrationReport.printGlobalReport(context.logger)
    }

    private static List<ProcessDefinition> getProcesses(MigrationContext context) {
        context.sql.rows("SELECT tenantid,processId,name,version FROM process_definition").collect {
            new ProcessDefinition(it.tenantid, it.processId, it.name, it.version)
        }
    }

    def updateSequenceTable(MigrationContext migrationContext) {
        dependencySequences.each { entry ->
            migrationContext.sql.executeUpdate("""UPDATE sequence SET nextid=${
                entry.value
            } WHERE id = 10 AND tenantid=${entry.key}""")
        }
        dependencyMappingSequences.each { entry ->
            migrationContext.sql.executeUpdate("""UPDATE sequence SET nextid=${
                entry.value
            } WHERE id = 11 AND tenantid=${entry.key}""")
        }
    }

    def initializeSequences(MigrationContext context) {
        context.sql.rows("""SELECT nextid, tenantid FROM sequence WHERE id = 10""").each { sequence ->
            dependencySequences.put(longValue(sequence.tenantid), longValue(sequence.nextid))
        }
        context.sql.rows("""SELECT nextid, tenantid FROM sequence WHERE id = 11""").each { sequence ->
            dependencyMappingSequences.put(longValue(sequence.tenantid), longValue(sequence.nextid))
        }
    }

    private static long longValue(def nextid) {
        long value
        if (nextid instanceof Number) {
            value = nextid.longValue()
        } else {
            value = nextid
        }
        value
    }

    private void migrateConnector(ProcessDefinition process, String sourceRepository, MigrationContext context, String connectorNamePattern, String... newImplFileNames) {
        def newConnectorImplementations = newImplFileNames.collect() {
            toConnectorImplementation(it, sourceRepository)
        }

        def implementationsToReplace = getConnectorImplementationMatching(context, process, connectorNamePattern, newConnectorImplementations)
        if (implementationsToReplace.empty) {
            return
        }
        def (List<String> dependenciesToRemove, List<String> dependenciesToAdd) =
        getDependenciesToAddAndRemove(implementationsToReplace, newConnectorImplementations, context, process)

        def connectorsUsingDependencies = getConnectorsThatAreUsingDependencies(context, process, implementationsToReplace, dependenciesToRemove)
        if (!connectorsUsingDependencies.empty) {
            connectorMigrationReport.reportFailureCausedByDependencyError(process, newConnectorImplementations, connectorsUsingDependencies, dependenciesToRemove)
            return
        }
        replaceConnectorImplementationFiles(implementationsToReplace, newConnectorImplementations, context, process, sourceRepository)
        replaceDependencies(context, process, dependenciesToRemove, dependenciesToAdd, sourceRepository)
        connectorMigrationReport.reportSuccess(process, newConnectorImplementations, dependenciesToRemove, dependenciesToAdd)

    }

    private ConnectorImplementation toConnectorImplementation(it, sourceRepository) {
        def connectorImplementation = ConnectorImplementation.parseConnectorXml(
                this.getClass().getResourceAsStream("/version/to_7_9_0/${sourceRepository}/${it}").text)
        connectorImplementation.fileName = it
        connectorImplementation
    }

    private static List<List<String>> getDependenciesToAddAndRemove(List<ConnectorImplementation> implementationsToReplace, List<ConnectorImplementation> newConnectorImplementations, MigrationContext context, ProcessDefinition process) {
        List<String> newDependencies = newConnectorImplementations.first().jarDependencies
        List<String> oldDependencies = implementationsToReplace.collect { it.jarDependencies }.flatten().toSet().toList()

        List<String> dependenciesToRemove = oldDependencies.findAll { !newDependencies.contains(it) }
        List<String> dependenciesToAdd = newDependencies.findAll { !oldDependencies.contains(it) }
        dependenciesToAdd.removeAll(getAllDependenciesOfProcess(context, process.tenantId, process.id))
        [dependenciesToRemove, dependenciesToAdd]
    }

    private static List<ConnectorImplementation> getConnectorsThatAreUsingDependencies(MigrationContext context, ProcessDefinition process, implementationsToReplace, dependenciesToRemove) {
        def otherImplementations = getConnectorImplementations(context, process).findAll {
            !implementationsToReplace.id.contains(it.id)
        }

        otherImplementations.findAll { connector ->
            connector.jarDependencies.any { dep -> dependenciesToRemove.contains(dep) }
        }
    }


    private void replaceDependencies(MigrationContext context, ProcessDefinition process, List<String> dependenciesToRemove, List<String> dependenciesToAdd, sourceRepository) {
        removeDependenciesOfConnectorImplementation(context, process.id, process.tenantId, dependenciesToRemove)

        dependenciesToAdd.each { dependencyName ->
            insertDependencyForProcess(context, process.id, process.tenantId, dependencyName, sourceRepository)
        }
    }

    private List<ConnectorImplementation> replaceConnectorImplementationFiles(List<ConnectorImplementation> implementationsToReplace, List<ConnectorImplementation> newConnectorImplementations, MigrationContext context, process, sourceRepository) {
        implementationsToReplace.each { implementationToReplace ->
            Object newConnectorImplementation = getNewImplementationOf(implementationToReplace, newConnectorImplementations)
            if (newConnectorImplementation != null) {
                replaceConnectorImplementation(context, process.tenantId, implementationToReplace.resourceId, process.id, implementationToReplace.fileName, getResource(sourceRepository, newConnectorImplementation.fileName), newConnectorImplementation.fileName)
            } else {
                context.logger.error("There was no matching implementation found for the connector " + implementationToReplace.fileName + ". Aborting the migration. This should never happen.")
                throw new RuntimeException()
            }
        }
    }

    static ConnectorImplementation getNewImplementationOf(ConnectorImplementation implementationToReplace, List<ConnectorImplementation> newConnectorImplementations) {
        def newConnectorImplementation = newConnectorImplementations.find {
            it.definitionId == implementationToReplace.definitionId
        }
        newConnectorImplementation
    }

    private List<ConnectorImplementation> getConnectorImplementationMatching(MigrationContext context, ProcessDefinition process, connectorNamePattern, List<ConnectorImplementation> newConnectorImplementations) {
        def newDefinitionIds = newConnectorImplementations.collect { it.definitionId }
        context.sql.rows(""" SELECT id, name, type, content FROM bar_resource b WHERE b.tenantid = ${process.tenantId}
 AND b.process_id = ${process.id} AND b.type = 'CONNECTOR' AND name LIKE '%${connectorNamePattern}%' """.toString())
                .collect {
                    try {
                        def implementation = ConnectorImplementation.parseConnectorXml(getText(it.content))
                        implementation.fileName = it.name
                        implementation.resourceId = it.id
                        implementation
                    } catch (SAXParseException ignored) {
                        context.logger.error("Cannot parse the implementation of connector ${it.name}. Ignoring. You may want to migrate the connector manually, or remove it from the database.")
                        new ConnectorImplementation()
                    }
                }.findAll { newDefinitionIds.contains(it.definitionId) }
                .findAll { it.definitionVersion == newConnectorImplementations.first().definitionVersion }
                .findAll {
                    if (it.semanticVersion < newConnectorImplementations.first().semanticVersion) {
                        return true
                    } else {
                        connectorMigrationReport.reportGenericFailure(process, it, "Version of connector $it.definitionId is already up-to-date.")
                        return false
                    }
                }
    }

    private static String getText(def content) {
        if (content instanceof Blob) {
            return new String(content.binaryStream.text)
        }
        new String(content)
    }

    private insertDependencyForProcess(MigrationContext context, long processId, long tenantId, String dependencyFilename, String sourceRepository) {
        def newName = processId.toString() + '_' + dependencyFilename
        def nextId = nextDependencyId(tenantId)
        def theJarToInsert = getResource(sourceRepository, dependencyFilename)
        context.sql.executeInsert("""INSERT INTO dependency(TENANTID, ID,NAME,DESCRIPTION,FILENAME,VALUE_) VALUES ($tenantId,$nextId,$newName,'',$dependencyFilename,$theJarToInsert)""")
        context.sql.executeInsert("""INSERT INTO dependencymapping(TENANTID,ID,ARTIFACTID,ARTIFACTTYPE,DEPENDENCYID) VALUES ($tenantId,${
            nextDependencyMappingId(tenantId)
        },$processId,'PROCESS',$nextId)""")
    }

    private byte[] getResource(String sourceRepository, String fileName) {
        this.getClass().getResourceAsStream("/version/to_7_9_0/$sourceRepository/$fileName").bytes
    }

    private long nextDependencyId(long tenantid) {
        def nextId = dependencySequences.get(tenantid)
        dependencySequences.put(tenantid, nextId + 1)
        return nextId
    }

    private long nextDependencyMappingId(long tenantid) {
        def nextId = dependencyMappingSequences.get(tenantid)
        dependencyMappingSequences.put(tenantid, nextId + 1)
        return nextId
    }

    private static List<String> removeDependenciesOfConnectorImplementation(MigrationContext context, processId, tenantId, List<String> dependencies) {
        dependencies.each { dependencyFileName ->
            List<Long> dependencyIds = context.sql.rows("""SELECT de.id 
        FROM dependency de, dependencymapping dm 
        WHERE dm.tenantid = $tenantId 
        AND de.tenantid = $tenantId 
        AND de.id = dm.dependencyid 
        AND de.filename = $dependencyFileName
        AND dm.artifactid = $processId 
        AND dm.artifactType = 'PROCESS'""").id.collect { longValue(it) }
            if (!dependencyIds.empty) {
                def dependencyId = dependencyIds.first()
                context.sql.execute("DELETE FROM dependency where tenantId = $tenantId AND id = $dependencyId")
            }
        }
    }

    private static void replaceConnectorImplementation(MigrationContext context, tenantId, id, processId, String implFileName, byte[] newImplFile, String newImplFileName) {
        context.sql.execute("""DELETE FROM bar_resource WHERE tenantId = $tenantId AND id = $id AND process_Id = $processId AND name = $implFileName  """)
        context.sql.executeInsert("""INSERT INTO bar_resource(TENANTID, ID, PROCESS_ID, NAME, TYPE, CONTENT) VALUES ($tenantId,$id,$processId,$newImplFileName,'CONNECTOR',$newImplFile)""")
    }

    private static List<ConnectorImplementation> getConnectorImplementations(MigrationContext context, ProcessDefinition process) {
        return context.sql.rows(""" SELECT name,content FROM bar_resource b 
WHERE b.tenantid = ${process.tenantId} AND b.process_id = ${process.id} AND b.type = 'CONNECTOR'""").collect {
            ConnectorImplementation.parseConnectorXml(getText(it.content))
        }
    }

    private static List<String> getAllDependenciesOfProcess(MigrationContext context, long tenantId, long processId) {
        context.sql.rows("""
SELECT d.fileName FROM dependency d, dependencymapping dm 
WHERE dm.artifactid = $processId AND dm.artifactType = 'PROCESS' AND d.tenantid = $tenantId  AND d.id = dm.dependencyid AND dm.tenantid = $tenantId""").fileName
    }

    @Override
    String getDescription() {
        return "Replace the connector definition for WebService, CMIS & Email for Java 11 compatibility"
    }

    @Override
    String getWarning() {
        connectorMigrationReport.hasFailure() ? connectorMigrationReport.getFailureReport() : null
    }
}

package org.bonitasoft.migration.version.to7_9_0


import groovy.sql.GroovyRowResult
import org.bonitasoft.migration.core.MigrationContext

import java.sql.Blob

/**
 * @author Danila Mazour
 */
class ConnectorTestUtils {

    MigrationContext migrationContext;

    long currentId = 1

    void insertConnectorDependency(String jarName, long processDefinitionId) {
        def theJarFile = jarName.bytes
        migrationContext.sql.executeInsert """INSERT INTO dependency 
(TENANTID, ID, NAME, DESCRIPTION, FILENAME, VALUE_)
VALUES(1, $currentId, ${processDefinitionId + '_' + jarName},'', $jarName, $theJarFile)"""
        migrationContext.sql.executeInsert("""INSERT INTO dependencymapping(TENANTID,ID,ARTIFACTID,ARTIFACTTYPE,DEPENDENCYID) VALUES (1,$currentId,$processDefinitionId,'PROCESS',$currentId)""")
        currentId++
        migrationContext.sql.executeUpdate("""UPDATE sequence SET nextid=$currentId WHERE id = 10""")
        migrationContext.sql.executeUpdate("""UPDATE sequence SET nextid=$currentId WHERE id = 11""")
    }

    void createProcessDefinition(long processDefinitionId, String name, String version){
        migrationContext.sql.executeInsert("INSERT INTO process_definition(TENANTID,PROCESSID, NAME, VERSION) VALUES (1,$processDefinitionId, $name, $version)")
    }

    void insertConnectorImplementation(String implName, long processDefinitionId) {
        def theImplFile = getContentOfImplFile(implName)
        migrationContext.sql.executeInsert(""" INSERT INTO bar_resource(TENANTID, ID, PROCESS_ID, NAME, TYPE, CONTENT) 
VALUES (1,$currentId,$processDefinitionId,$implName,'CONNECTOR',$theImplFile)""")
        currentId++
    }

    byte[] getContentOfImplFile(String implName) {
        this.getClass().getResourceAsStream("/impl/${implName}").bytes
    }

    byte [] getJarFile(String jarName){
        this.getClass().getResourceAsStream("/version/to_7_9_0/${jarName}").bytes
    }

    List<GroovyRowResult>  getDependenciesOfProcess(long processDefinitionId){
        migrationContext.sql.rows("""SELECT d.tenantid, d.id, d.name, d.description, d.filename, d.value_ FROM dependency d, dependencymapping dm 
WHERE dm.artifacttype = 'PROCESS' AND dm.artifactid = ${processDefinitionId} AND dm.dependencyId = d.id AND d.tenantid = dm.tenantid ORDER BY name""".toString())
    }

    List getContentResourcesOfProcess(long processDefinitionId){
        migrationContext.sql.rows("""SELECT tenantid,id,process_id,name,type,content FROM bar_resource WHERE process_id = ${processDefinitionId} ORDER BY id""").content.collect{(it instanceof Blob)?it.binaryStream.bytes:it}
    }

    long getNextIdOfSequence(long sequenceId){
        migrationContext.sql.firstRow("""SELECT nextid FROM sequence WHERE id = ${sequenceId}""").nextid
    }

}

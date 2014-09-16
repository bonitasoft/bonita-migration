/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
import org.bonitasoft.migration.versions.v6_2_6to_6_3_0.ProcessDefinition
import org.bonitasoft.migration.versions.v6_2_6to_6_3_0.TransientData

//list process definitions
sql.eachRow("SELECT * from process_definition", { row ->
    def tenantId = row[0]
    def id = row[2]
    migrateDefinition(tenantId instanceof BigDecimal ? tenantId.longValue() : tenantId, id instanceof BigDecimal ? id.longValue() : id)

})

def migrateDefinition(long tenantId, long id) {
    def s = File.separator
    def File clientProcessFile = new File(bonitaHome.getAbsolutePath() + "${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}process-design.xml")
    //we migrate the client process xml only if the migration is not done yet (see BS-10016 )
    if(clientProcessFile.text.contains("http://www.bonitasoft.org/ns/process/client/6.0")){
        migrateProcessFile(clientProcessFile)
    }

}

private ProcessDefinition migrateProcessFile(File processFile) {
    def ProcessDefinition processDefinition = new ProcessDefinition(processFile.text, false)
    println "Update operations and expression of client process xml file " + processDefinition.getName() + "--" + processDefinition.getVersion()
    //parse process to get list all transient data
    def transientData = processDefinition.getTransientData()

    //Update Operations to add left operand type and to convert left operand that update transient data to have the good type
    processDefinition.updateOperatorAndLeftOperandType(transientData)
    transientData.each { TransientData data ->
        //  Change type of expression that evaluate transient data
        processDefinition.updateExpressionOf(data)
    }
    processFile.withWriter("UTF-8") { it << processDefinition.getContent() }
    processDefinition
}






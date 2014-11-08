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
import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.versions.v6_2_6_to_6_3_0.Forms;
import org.bonitasoft.migration.versions.v6_2_6_to_6_3_0.ProcessDefinition;
import org.bonitasoft.migration.versions.v6_2_6_to_6_3_0.TransientData;

def formsVersion = new File(feature,"FORMS_VERSION").text
//list process definitions
sql.eachRow("SELECT * from process_definition", { row ->
    def tenantId = row[0]
    def id = row[2]
    migrateDefinition(tenantId instanceof BigDecimal? tenantId.longValue():tenantId, id instanceof BigDecimal? id.longValue():id, formsVersion)

})



//delete datamapping of transient data
MigrationUtil.getTenantsId(dbVendor, sql).each{
    sql.executeUpdate("DELETE FROM data_mapping WHERE tenantid = $it AND datainstanceid NOT IN (SELECT id FROM data_instance WHERE tenantid = $it)");
}
//drop datasource tables
MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql)

def migrateDefinition(long tenantId, long id, String formsVersion){
    def s = File.separator
    def File processFile = new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}server-process-definition.xml")
    ProcessDefinition processDefinition = migrateProcessFile(processFile, true)
    def File clientProcessFile = new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}process-design.xml")
    migrateProcessFile(clientProcessFile, false)

    //update forms.xml
    def File formsxml = new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}resources${s}forms${s}forms.xml")

    if(formsxml.exists()){
        println "Update operations and expression of forms "
        def Forms forms = new Forms(formsxml.text, processDefinition.getTransientData(), formsVersion)
        //update  expressions: each TYPE_VARIABLE that point to a transient data are changed to TYPE_TRANSIENT_VARIABLE
        forms.updateExpressions()
        //update operations: add type on left operand and change the operation type
        forms.updateActions()

        formsxml.withWriter("UTF-8") { it << forms.getContent() }
    }else{
        println "no forms.xml for process $processDefinition.name -- $processDefinition.version ($processDefinition.id)"
    }

    //delete cached forms.xml in client bonita home
    def cacheFile = new File(bonitaHome.getAbsolutePath()+"${s}client${s}tenants${s}${tenantId}${s}work${s}forms${s}${processDefinition.name}--${processDefinition.version}")
    if(cacheFile.exists()){
        cacheFile.deleteDir()
    }
}

private ProcessDefinition migrateProcessFile(File processFile, boolean isServer) {
    def ProcessDefinition processDefinition = new ProcessDefinition(processFile.text, isServer)
    println "Update operations and expression of process " + processDefinition.getName() + "--" + processDefinition.getVersion()
    //parse process to get list all transient data
    def transientData = processDefinition.getTransientData()

    //warn for each transient data without initial value
    transientData.each { TransientData data ->
        if (!data.haveInitialValue) {
            println "Transient data < $transientData.name > of process $processDefinition.id $processDefinition.name -- $processDefinition.version having id $transientData.containerId must have an initial value. Please update it to make sure it will work in further versions"
        }
    }
    //Update Operations to add left operand type and to convert left operand that update transient data to have the good type
    processDefinition.updateOperatorAndLeftOperandType(transientData)
    transientData.each { TransientData data ->
        //  Change type of expression that evaluate transient data
        processDefinition.updateExpressionOf(data)
    }
    processFile.withWriter("UTF-8") { it << processDefinition.getContent() }
    processDefinition
}






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
import groovy.sql.Sql;

import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.versions.v6_2_6to_6_3_0.ProcessDefinition;
import org.bonitasoft.migration.versions.v6_2_6to_6_3_0.TransientData;


//list process definitions
sql.eachRow("SELECT * from process_definition",[false], { row ->
    migrateDefinition(row[0], row[1])
    
})
//delete datamapping of transient data
sql.executeUpdate("DELETE FROM data_mapping m WHERE datainstanceid NOT IN (SELECT id FROM data_instance d WHERE m.tenantid = d.tenantid)");
//for each process
def migrateDefinition(long tenantId, long id){
    def ProcessDefinition processDefinition = new ProcessDefinition(new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${tenantId}${s}work${s}processes${s}${id}${s}server-process-definition.xml").text)
    
    //parse process to get list all transient data
    def transientData = processDefinition.getTransientData()
    
    //warn for each transient data without initial value
    transientData.each { TransientData data ->
        if( !data.haveInitialValue ){
            println "Transient data < $transientData.name > of process $processDefinition.id $processDefinition.name $processDefinition.version having id $transientData.containerId must have an initial value. Please update it to make sure it will work in further versions"
        }
        
    }
    //Update Operations to add left operand type
    processDefinition.updateOperatorAndLeftOperandType(transientData)
    transientData.each { TransientData data ->
    //  Change type of expression that evaluate transient data
        processDefinition.updateExpressionOf(data)
        //Convert left operand that update transient data to have the good type
    }
    
    
    
    
    
    def flownode = processDefinition.depthFirst().grep{ it.name.localPart.endsWith("DataDefinition") && it.@transient == "true" }
    
    
    def groovy.xml.QName tag =  flownode.name()
    def type = tag.localPart
    def flownodeDef = new FlowNodeDefinition(id:flownode.@id,name:flownode.@name, type:type)
    if(flownodeDef.isGateway()){
        flownodeDef.gateType = flownode.@gatewayType
        flownode.incomingTransitions.each {
            def idref = it.@idref
            flownodeDef.addTransition(processDefinition.depthFirst().grep{ it.@id == idref }[0].@name)
        }
    }
}






import groovy.sql.Sql;

import java.io.File;
import java.util.Map;

import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.GatewayInstance;
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.TransitionInstance;
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.FlowNodeDefinition;



/*
 * Migrate a single transition instance
 */
public migrateTransition(TransitionInstance transition, File feature, Map flownodeIdsByTenants){
    //get the definition
    println "migration of transition <$transition.name> with id <$transition.id> of process definition <$transition.processDefId>"
    def s = File.separatorChar;
    def processDefXml = new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${transition.tenantid}${s}work${s}processes${s}${transition.processDefId}${s}server-process-definition.xml");
    def FlowNodeDefinition target = getTargetOfTransition(processDefXml.text, transition)
    //if target = gateway, create or hit the gateway
    if(target.isGateway()){
        // check merging condition: if merge set as finished
        switch(target.gateType){
            case "PARALLEL":
            //get the gateway or create it
            //select the correponding gateway:
                def GatewayInstance gateway = getGateway(transition,target);
                def hitBys = "FINISH:1";
                def nbIncomingTransition = target.incommingTransitions.size()
                def transitionIndex = target.incommingTransitions.indexOf(transition.name) +1 // index of transition start at 1
                if(gateway == null){//create new gateway
                    if( nbIncomingTransition > 1 ){//only 1 incomming: the gateway is finished, more than one: put in hitBys the transition index in definition
                        hitBys = transitionIndex;
                    }
                    println "Insert new gateway with hitBys = '"+hitBys+"'";
                    insertFlowNode(transition, target,feature,flownodeIdsByTenants, [":gatewayType":"'PARALLEL'",":hitBys":"'"+hitBys+"'"]);
                }else{//update the existing gateway
                    hitBys = gateway.hitBys;
                    def nbHit = hitBys.split(",").length +1
                    def isFinished = nbHit == nbIncomingTransition
                    //is finished if all tr are here
                    updateGateway(isFinished, hitBys, nbHit, transitionIndex, gateway);
                }
                break;
            case "EXCLUSIVE":
            //create the gateway in finished
                insertFlowNode(transition, target,feature,flownodeIdsByTenants, [":gatewayType":"'EXCLUSIVE'",":hitBys":"'FINISH:1'"]);
                break;
            case "INCLUSIVE":
            //select the correponding gateway:
                def GatewayInstance gateway = getGateway(transition,target);
                def hitBys = "FINISH:1";
                def nbIncommingTransition = target.incommingTransitions.size()
                def transitionIndex = target.incommingTransitions.indexOf(transition.name) +1 // index of transition start at 1
                if(gateway == null){//create new gateway for this transition
                    if( nbIncommingTransition > 1 && getNumberOfTokens(transition.tenantid,transition.parentContainerId,transition.tokenRefId) > 1){
                        //more than one incomming AND this branch is not the only one active (nb token >1): the gateway is not finished
                        hitBys = transitionIndex;
                    }
                    println "Insert new gateway with hitBys = '"+hitBys+"'";
                    insertFlowNode(transition, target,feature,flownodeIdsByTenants, [":gatewayType":"'INCLUSIVE'",":hitBys":"'"+hitBys+"'"]);
                }else{//update the existing gateway
                    hitBys = gateway.hitBys;
                    def nbHit = hitBys.split(",").length +1
                    def isFinished = nbHit == nbIncommingTransition || getNumberOfTokens(transition.tenantid,transition.parentContainerId,transition.tokenRefId)==nbHit
                    // isFinished if all transition hit the gateway or there is as much  token of that kind that hit the gateway
                    updateGateway(isFinished, hitBys, nbHit, transitionIndex, gateway);
                }
                break;
        }
    }else{
        //if target is not a gateway create the element
        println "Insert a flow node";
        insertFlowNode(transition, target,feature,flownodeIdsByTenants, [:]);
    }
    sql.execute("DELETE FROM transition_instance WHERE id = $transition.id AND tenantid = $transition.tenantid")
    //archive transition
}
public updateGateway(def isFinished, def hitBys, def nbHit, def transitionIndex, def GatewayInstance gateway){
    if( isFinished ){
        hitBys = "FINISH:$nbHit"
    }else{//add the transition the hit the gateway
        hitBys = "$hitBys,$transitionIndex"
    }
    println "Update gateway <$gateway.id> with hitBys = '"+hitBys+"'"
    sql.executeUpdate("UPDATE flownode_instance SET hitBys = '"+hitBys+"' WHERE tenantId = $gateway.tenantid and id = $gateway.id")
}
public GatewayInstance getGateway(TransitionInstance transition, FlowNodeDefinition target){
    def GatewayInstance gateway = null;
    sql.eachRow("SELECT * FROM flownode_instance WHERE tenantid = $transition.tenantid AND parentContainerId = $transition.parentContainerId AND flownodeDefinitionId = $target.id",{row ->
        if(gateway == null){//if not found
            if(!row.hitBys.contains("$transition.name") && !row.hitBys.contains("FINISH:")){//if the transition already hit this gateway, create a new one
                gateway = toGateway(row)
            }
        }
    });
    return gateway;
}
private int getNumberOfTokens(tenantid, processInstanceId,ref_id){
    def result = 0
    sql.eachRow("SELECT count(id) FROM token WHERE tenantid = $tenantid AND processInstanceId = $processInstanceId AND ref_id = $ref_id",{row -> result = row[0]})
    if(result == 0){
        throw new IllegalStateException("Unable to migrate transition because there is no token for this transition")
    }
    return result;
}
private GatewayInstance toGateway(row){
    return new GatewayInstance(tenantid:row.tenantid,
    id:row.id,
    flownodeDefinitionId:row.flownodeDefinitionId,
    kind:row.kind,
    rootContainerId:row.rootContainerId,
    parentContainerId:row.parentContainerId,
    name:row.name,
    stateId:row.stateId,
    prev_state_id:row.prev_state_id,
    stateName:row.stateName,
    terminal:row.terminal,
    stable:row.stable,
    stateCategory:row.stateCategory,
    gatewayType:row.gatewayType,
    hitBys:row.hitBys,
    logicalGroup1:row.logicalGroup1,
    logicalGroup2:row.logicalGroup2,
    logicalGroup3:row.logicalGroup3,
    logicalGroup4:row.logicalGroup4,
    tokenCount:row.tokenCount,
    token_ref_id:row.token_ref_id);
}
public void insertFlowNode(TransitionInstance transition, FlowNodeDefinition target, File feature, Map flownodeIdsByTenants, Map overrideParameters){
    def nextId = flownodeIdsByTenants.get(transition.tenantid);
    def Map parameters = [":tenantid":transition.tenantid,":id":nextId,":flownodeDefinitionId":target.getId(),":kind":target.type,":rootContainerId":transition.rootContainerId,":parentContainerId":transition.parentContainerId,
        ":name":target.name,":stateId":target.getStateId(),":stateName":target.getStateName(),":stateCategory":"NORMAL", ":logicalGroup1":transition.processDefId,":logicalGroup2":transition.rootContainerId,":logicalGroup3":"0",
        ":logicalGroup4":transition.parentContainerId,":token_ref_id":transition.tokenRefId,":gatewayType":null,":hitBys":null]
    parameters.putAll(overrideParameters)
    MigrationUtil.executeSqlFile(feature, dbVendor, "insertFlowNode", parameters, sql, false)
    flownodeIdsByTenants.put(transition.tenantid, nextId+1)
}

public Object getTargetOfTransition(String processDefXml, TransitionInstance transition) {
    def processDefinition = new XmlParser().parseText(processDefXml);
    def targetDefId = processDefinition.flowElements.transitions.transition.find{it.@name==transition.name}.@target
    def groovy.util.Node flownode = processDefinition.depthFirst().grep{ it.@id == targetDefId }[0]

    def groovy.xml.QName tag =  flownode.name()
    def type = tag.localPart
    def flownodeDef = new FlowNodeDefinition(id:flownode.@id,name:flownode.@name, type:type)
    if(flownodeDef.isGateway()){
        flownodeDef.gateType = flownode.@gatewayType
        flownode.incomingTransition.each {
            def idref = it.@idref
            flownodeDef.addTransition(processDefinition.depthFirst().grep{ it.@id == idref }[0].@name)
        }
    }
    return flownodeDef
}

//get next flow node id by tenant
def flownodeIdsByTenants = [:];

sql.eachRow("SELECT tenantid,nextId from sequence WHERE id = 10011") { row ->
    flownodeIdsByTenants.put(row[0],row[1]) }
println "next id by tenants "+flownodeIdsByTenants;
//get all transitions
sql.eachRow("SELECT * from transition_instance", { row ->
    def transition = new TransitionInstance(tenantid:row[0], id:row[1], rootContainerId:row[2], parentContainerId:row[3], name:row[4],source:row[5], processDefId:row[9], tokenRefId:row[15]);
    migrateTransition(transition, feature, flownodeIdsByTenants);

})
//update sequence for the elements
flownodeIdsByTenants.each {
    println "update sequence of tenant $it.key  for flow nodes to $it.value"
    println  sql.executeUpdate("UPDATE sequence SET nextId = $it.value WHERE tenantId = $it.key and id = 10011")+" row(s) updated";
}
//delete the table
println "deleting transition instance table..."
MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);

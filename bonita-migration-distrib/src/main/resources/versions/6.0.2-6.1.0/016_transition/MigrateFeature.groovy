import java.io.File;
import java.util.Map;

import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.TransitionInstance;
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.FlowNodeDefinition;



/*
 * Migrate a single transition instance
 */
public migrateTransition(TransitionInstance transition, File feature, Map flownodeIdsByTenants){
    //get the definition
    println "migration of transition $transition"
    def s = File.separatorChar;
    def processDefXml = new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${transition.tenantid}${s}work${s}processes${s}${transition.processDefId}${s}server-process-definition.xml");
    def FlowNodeDefinition target = getTargetOfTransition(processDefXml.text, transition)
    println "target is $target"
    //if target = gateway, create or hit the gateway
    if(target.isGateway()){
        // check merging condition: if merge set as finished
        switch(target.type){
            case "PARALLEL":
            //get the gateway or create it
                println "Detected parallel gateway"
                break;
            case "EXCLUSIVE":
            //create the gateway in finished
                println "Detected exclusive gateway"
                break;
            case "INCLUSIVE":
                println "Detected inclusive gateway"
                break;
        }
    }else{
        println "Detected flownode"
        //if target is not a gateway create the element
        def nextId = flownodeIdsByTenants.get(transition.tenantid);
        println "will insert flownode with id $nextId"
        def Map parameters = [":tenantid":transition.tenantid,":id":nextId,":flownodeDefinitionId":target.getId(),":kind":target.type,":rootContainerId":transition.rootContainerId,":parentContainerId":transition.parentContainerId,
            ":name":target.name,":stateId":target.getStateId(),":stateName":target.getStateName(),":stateCategory":"NORMAL", ":logicalGroup1":transition.processDefId,":logicalGroup2":transition.rootContainerId,":logicalGroup3":"0",
            ":logicalGroup4":transition.parentContainerId,":token_ref_id":transition.tokenRefId]
        MigrationUtil.executeSqlFile(feature, dbVendor, "insertFlowNode", parameters, sql, false)
        flownodeIdsByTenants.put(transition.tenantid, nextId+1)
    }
    //archive transition
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
MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);

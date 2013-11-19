import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.TransitionInstance;
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.FlowNodeDefinition;


return;
//get next flow node id by tenant
def flownodeIdsByTenants = [:];

//get all transitions
sql.query("SELECT * FROM transition_instance", { row ->
    def transition = new TransitionInstance(tenantid:row[0], id:row[1], rootContainerId:row[2], parentContainerId:row[3], name:row[4],source:row[5], processDefId:row[8], tokenRefId:row[14]);

    // Get the definition
    def s = File.separatorChar;
    def processDefXml = new File(bonitaHome.getAbsolutePath()+"${s}server${s}tenants${s}${transition.tenantid}${s}work${s}processes${s}${transition.processDefId}${s}server-process-definition.xml");

    // Get the target of transition
    def processDefinition = new XmlParser().parseText(processDefXml);
    def targetDefId = processDefinition.flowElements.transitions.transition.find{it.@id==transition.id}.@target
    def flownode = processDefinition.depthFirst().grep{ it.@id == targetDefId }[0]
    def type = (flownode.name() == "gateway"?flownode.@gatewayType:"flownode");
    def FlowNodeDefinition target = new FlowNodeDefinition(id:flownode.@id,name:flownode.@name, type:type);

    println "target of $transition is $target"
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
        //if target is not a gateway create the element
        println "Detected flownode"
    }
})
//update sequence for the elements
//delete the table
MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);
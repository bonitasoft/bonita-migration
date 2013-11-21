package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

import groovy.transform.EqualsAndHashCode


@EqualsAndHashCode
public class FlowNodeDefinition {

    def static Map types = ["userTask":"user",
        "manualTask":"manual",
        "automaticTask":"auto",
        "receiveTask":"receive",
        "sendTask":"send",
        "callActivity":"call",
        "subProcess":"subProc",
        "gateway":"gate",
        "startEvent":"startEvent",
        "intermediateCatchEvent":"intermediateCatchEvent",
        "intermediateThrowEvent":"intermediateThrowEvent",
        "endEvent":"endEvent"]
    def static Map stateIds = ["userTask":"32",
        "manualTask":"0",
        "automaticTask":"37",
        "receiveTask":"32",
        "sendTask":"37",
        "callActivity":"32",
        "subProcess":"31",
        "gateway":"61",
        "startEvent":"61",
        "intermediateCatchEvent":"0",
        "intermediateThrowEvent":"26",
        "endEvent":"26"]
    def static Map stateNames = ["userTask":"initializing",
        "manualTask":"initializing",
        "automaticTask":"executing",
        "receiveTask":"initializing",
        "sendTask":"executing",
        "callActivity":"initializing",
        "subProcess":"executing",
        "gateway":"executing",
        "startEvent":"executing",
        "intermediateCatchEvent":"initializing",
        "intermediateThrowEvent":"executing",
        "endEvent":"executing"]



    def String id;
    def String name;
    def String type;
    def String gateType;
    def List<String> incommingTransitions = [];


    public boolean isGateway(){
        return "gateway".equals(type)
    }

    String getDatabaseType(){
        return types.get(type);
    }

    /**
     *
     * @return the state name of the initial state
     */
    String getStateName(){
        return stateNames.get(type)
    }
    /**
     *
     * @return the state id of the initial state
     */
    String getStateId(){
        return stateIds.get(type)
    }

    @Override
    public String toString() {
        return "$type [id=$id, name=$name, type=$type, gateType=$gateType ]"+(incommingTransitions.size()>0?"transitions: $incommingTransitions":"");
    }

    public void addTransition(String idref){
        incommingTransitions.add(idref);
    }
}

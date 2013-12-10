/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

import groovy.transform.EqualsAndHashCode


/**
 *
 * @author Baptiste Mesta
 *
 */
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
    def List<String> incomingTransitions = [];


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
        return "$type [id=$id, name=$name, type=$type, gateType=$gateType ]"+(incomingTransitions.size()>0?"transitions: $incomingTransitions":"");
    }

    public void addTransition(String idref){
        incomingTransitions.add(idref);
    }
}

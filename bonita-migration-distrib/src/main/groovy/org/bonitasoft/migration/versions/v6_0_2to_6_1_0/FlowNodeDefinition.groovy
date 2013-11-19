package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@EqualsAndHashCode
@ToString
public class FlowNodeDefinition {

    def String id;
    def String name;
    def String type;


    boolean isGateway(){
        return type != "flownode"
    }
}

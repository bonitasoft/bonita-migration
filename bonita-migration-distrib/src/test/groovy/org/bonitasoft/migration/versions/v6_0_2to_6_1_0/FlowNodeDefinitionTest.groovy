package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

import static org.junit.Assert.*

import org.junit.Test


class FlowNodeDefinitionTest {

    @Test
    public void isNotGateway(){
        assertFalse(new FlowNodeDefinition(type:"flownode").isGateway());
    }
    @Test
    public void isGateway(){
        assertTrue(new FlowNodeDefinition(type:"PARALLEL").isGateway());
    }
}

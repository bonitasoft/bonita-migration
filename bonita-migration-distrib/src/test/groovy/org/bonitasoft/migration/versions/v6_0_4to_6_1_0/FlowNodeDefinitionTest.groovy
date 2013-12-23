package org.bonitasoft.migration.versions.v6_0_4to_6_1_0;

import static org.junit.Assert.*

import org.bonitasoft.migration.versions.v6_0_4to_6_1_0.FlowNodeDefinition;
import org.junit.Test


class FlowNodeDefinitionTest {

    @Test
    public void isNotGateway(){
        assertFalse(new FlowNodeDefinition(type:"flownode").isGateway());
    }
    @Test
    public void isGateway(){
        assertTrue(new FlowNodeDefinition(type:"gateway").isGateway());
    }
}

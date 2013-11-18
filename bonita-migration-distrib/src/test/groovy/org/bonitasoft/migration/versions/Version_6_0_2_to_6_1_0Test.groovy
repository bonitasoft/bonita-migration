package org.bonitasoft.migration.versions;

import static org.junit.Assert.*
import static org.mockito.Matchers.*
import static org.mockito.Mockito.*
import groovy.sql.Sql

import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.FlowNodeDefinition
import org.bonitasoft.migration.versions.v6_0_2to_6_1_0.TransitionInstance
import org.junit.Ignore
import org.junit.Test


class Version_6_0_2_to_6_1_0Test {

    @Ignore("not finished")
    @Test
    public void testExecuteSqlFiles() throws Exception {
        def sql = mock(Sql.class);
        def version = new Version_6_0_2_to_6_1_0()
        def resources = new File("resources")
        resources.mkdir()
        //        def createSubFolder= {parent,name->new File()}
        //        resources.
        //
        //        version.executeSqlFiles(null, "", null)
    }

    @Test
    public void getTargetOfTransition() throws Exception {
        def process ='''<?xml version="1.0" encoding="UTF-8"?>
<processDefinition bos_version="6.0-SNAPSHOT" id="7219115537128996651" name="ProcessWithTransitions" version="1.0">
  <stringIndexes>
    <stringIndex index="1"/>
  </stringIndexes>
  <flowElements>
    <transitions>
      <transition id="9019593255021295063" name="step1_-&gt;_gate2" source="-8387720319258068830" target="-5109139435573341779"/>
      <transition id="1683913898656940302" name="gate2_-&gt;_end" source="-5109139435573341779" target="-8558673934546753353"/>
      <transition id="6962317490942068176" name="gate1_-&gt;_step1" source="-8326511714356376685" target="-8387720319258068830"/>
      <transition id="5003153938338133660" name="gate1_-&gt;_step2" source="-8326511714356376685" target="-5297378610240677285"/>
      <transition id="1503154818783097379" name="step2_-&gt;_gate2" source="-5297378610240677285" target="-5109139435573341779"/>
      <transition id="2936169533980361644" name="start_-&gt;_gate1" source="-7467996157599684593" target="-8326511714356376685"/>
    </transitions>
    <connectors/>
    <dataDefinitions/>
    <documentDefinitions/>
    <flowNodes>
      <automaticTask id="-5297378610240677285" name="step2">
        <incomingTransition idref="5003153938338133660"/>
        <outgoingTransition idref="1503154818783097379"/>
        <dataDefinitions/>
        <operations/>
        <boundaryEvents/>
      </automaticTask>
      <automaticTask id="-8387720319258068830" name="step1">
        <incomingTransition idref="6962317490942068176"/>
        <outgoingTransition idref="9019593255021295063"/>
        <dataDefinitions/>
        <operations/>
        <boundaryEvents/>
      </automaticTask>
      <gateway gatewayType="PARALLEL" id="-5109139435573341779" name="gate2">
        <incomingTransition idref="9019593255021295063"/>
        <incomingTransition idref="1503154818783097379"/>
        <outgoingTransition idref="1683913898656940302"/>
      </gateway>
      <gateway gatewayType="PARALLEL" id="-8326511714356376685" name="gate1">
        <incomingTransition idref="2936169533980361644"/>
        <outgoingTransition idref="6962317490942068176"/>
        <outgoingTransition idref="5003153938338133660"/>
      </gateway>
      <startEvent id="-7467996157599684593" interrupting="true" name="start">
        <outgoingTransition idref="2936169533980361644"/>
      </startEvent>
      <endEvent id="-8558673934546753353" name="end">
        <incomingTransition idref="1683913898656940302"/>
      </endEvent>
    </flowNodes>
  </flowElements>
  <dependencies>
    <parameters/>
    <actors/>
  </dependencies>
</processDefinition>
        ''';
        def version = new Version_6_0_2_to_6_1_0();

        assertEquals(new FlowNodeDefinition(id:"-5109139435573341779",name:"gate2",type:"PARALLEL"),version.getTargetOfTransition(process, new TransitionInstance(id:"1503154818783097379")));
        assertEquals(new FlowNodeDefinition(id:"-8387720319258068830",name:"step1",type:"flownode"),version.getTargetOfTransition(process, new TransitionInstance(id:"6962317490942068176")));
        assertEquals(new FlowNodeDefinition(id:"-8558673934546753353",name:"end",type:"flownode"),version.getTargetOfTransition(process, new TransitionInstance(id:"1683913898656940302")));
    }
}

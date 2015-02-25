package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0

import static org.assertj.core.api.Assertions.*

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.junit.Test


class ProcessDefinitionTest {
    private static final def after = ProcessDefinitionTest.class.getResourceAsStream("after.xml").text
    private static final def before = ProcessDefinitionTest.class.getResourceAsStream("before.xml").text
    private static final def beforeWithProcessOperation = ProcessDefinitionTest.class.getResourceAsStream("before-with-process-operation.xml").text

    @Test
    public void testGetTransientData() throws Exception {
        //given
        def pdp = new ProcessDefinition(before,true)

        //when
        def List<TransientData> transientData = pdp.getTransientData()

        //then
        assertThat(transientData.size).isEqualTo(4)
        assertThat(transientData[0].name).isEqualTo("transientData")
        assertThat(transientData[0].containerId).isEqualTo(-4865307453020359312)
        assertThat(transientData[0].haveInitialValue).isEqualTo(true)
        assertThat(transientData[1].name).isEqualTo("transientDataBlob")
        assertThat(transientData[1].haveInitialValue).isEqualTo(true)
        assertThat(transientData[2].name).isEqualTo("transientData2")
        assertThat(transientData[2].haveInitialValue).isEqualTo(false)
        assertThat(transientData[3].name).isEqualTo("transientData")
        assertThat(transientData[3].containerId).isEqualTo(-8502126897306972837)
    }
    @Test
    public void getAttributes() throws Exception {
        //when
        def pdp = new ProcessDefinition(before, true)

        //then
        assertThat(pdp.id).isEqualTo(4725507608188665945)
        assertThat(pdp.name).isEqualTo("MyProcess")
        assertThat(pdp.version).isEqualTo("1.0")
    }
    @Test
    public void should_updateNameSpace_update_the_ns() throws Exception {

        //when
        def pdp = new ProcessDefinition(before, true)

        //then
        assertThat(pdp.processDefinitionXml.name().namespaceURI).isEqualTo('''http://www.bonitasoft.org/ns/process/server/6.3''')
    }

    @Test
    public void should_updateNameSpace_update_the_ns_on_client() throws Exception {

        //when
        def pdp = new ProcessDefinition(before.replace("http://www.bonitasoft.org/ns/process/server/6.0","http://www.bonitasoft.org/ns/process/client/6.0"), false)

        //then
        assertThat(pdp.processDefinitionXml.name().namespaceURI).isEqualTo('''http://www.bonitasoft.org/ns/process/client/6.3''')
    }


    @Test
    public void should_updateExpressionOf_update_the_type() throws Exception {
        //given
        def ProcessDefinition pdp = new ProcessDefinition(before, true)
        def data = pdp.getTransientData()[0]

        //when
        pdp.updateExpressionOf(data)

        //then: there is 3 updated expression in this activity
        assertThat(pdp.processDefinitionXml.depthFirst().findAll{ it.@expressionType == "TYPE_TRANSIENT_VARIABLE" && it.@name == "transientData" }.size).isEqualTo(3)
    }

    @Test
    public void should_updateOperatorAndLeftOperandType_update_the_type() throws Exception {
        //given
        def ProcessDefinition pdp = new ProcessDefinition(before, true)

        //when
        pdp.updateOperatorAndLeftOperandType(pdp.getTransientData())

        assertThat(pdp.processDefinitionXml.depthFirst().findAll{ it.@type == null && it.name().getLocalPart() == "leftOperand" }.size).isEqualTo(0)
        assertThat(pdp.processDefinitionXml.depthFirst().findAll{ it.@type == "DOCUMENT" && it.name().getLocalPart() == "leftOperand" }.size).isEqualTo(1)
        assertThat(pdp.processDefinitionXml.depthFirst().findAll{ it.@operatorType =="DOCUMENT_CREATE_UPDATE" && it.name().getLocalPart() == "operation" }.size).isEqualTo(0)
        assertThat(pdp.processDefinitionXml.depthFirst().findAll{ it.@type == "DATA" && it.name().getLocalPart() == "leftOperand" }.size).isEqualTo(6)
        assertThat(pdp.processDefinitionXml.depthFirst().findAll{ it.@type == "TRANSIENT_DATA" && it.name().getLocalPart() == "leftOperand" }.size).isEqualTo(2)
    }

    @Test //BS-10959
    public void can_migrate_process_with_operation_at_process_level() throws Exception {
        //given
        def ProcessDefinition pdp = new ProcessDefinition(beforeWithProcessOperation, true)

        //when
        pdp.updateOperatorAndLeftOperandType(pdp.getTransientData());
        pdp.getTransientData().each { TransientData data ->
            //  Change type of expression that evaluate transient data
            pdp.updateExpressionOf(data)
        }


        //then
        assertThat(pdp.processDefinitionXml.depthFirst().findAll{ it.@type == "DATA" && it.name().getLocalPart() == "leftOperand" }.size).isEqualTo(29)
    }

    @Test
    public void should_update_full_xml_is_equal_after(){

        def ProcessDefinition processDefinition = new ProcessDefinition(before, true)

        //parse process to get list all transient data
        def transientData = processDefinition.getTransientData()

        transientData.each { processDefinition.updateExpressionOf(it) }
        processDefinition.updateOperatorAndLeftOperandType(transientData)


        def content = processDefinition.getContent()
        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new Diff(after, content)
        assert xmlDiff.identical()
        //check formatting did not change also
        assertThat(content.trim()).isEqualTo(after)
    }
    
    @Test
    public void shouldMigrateLeftOperandTypeForInputAndOutputOperations() {
        def before = ProcessDefinitionTest.class.getResourceAsStream("1-before-server-process-definition.xml").text
        def after = ProcessDefinitionTest.class.getResourceAsStream("2-after-server-process-definition.xml").text
        
        def processDef = new ProcessDefinition(before, true)
        
        def transientData = processDef.getTransientData()

        processDef.updateOperatorAndLeftOperandType(transientData)

        def content = processDef.getContent()
        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new Diff(after, content)
        assert xmlDiff.identical()
        //check formatting did not change also
        assertThat(content.trim()).isEqualTo(after)
    }

    @Test
    public void should_migrate_client_work() {
        def before = ProcessDefinitionTest.class.getResourceAsStream("client_before.xml").text
        def after = ProcessDefinitionTest.class.getResourceAsStream("client_after.xml").text

        def processDef = new ProcessDefinition(before, false)

        def transientData = processDef.getTransientData()

        processDef.updateOperatorAndLeftOperandType(transientData)

        assertThat(processDef.id).isEqualTo("")


        def content = processDef.getContent()
        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new Diff(after, content)
        assert xmlDiff.identical()
        //check formatting did not change also
        assertThat(content.trim()).isEqualTo(after)
    }

}

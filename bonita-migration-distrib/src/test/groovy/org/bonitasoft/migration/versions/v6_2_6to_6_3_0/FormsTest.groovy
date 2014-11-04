package org.bonitasoft.migration.versions.v6_2_6to_6_3_0;

import static org.assertj.core.api.Assertions.*
import static org.junit.Assert.*

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.junit.Test


class FormsTest {

    private static final def beforeProcessDef = ProcessDefinitionTest.class.getResourceAsStream("before-processdefwithform.xml").text
    private static final def beforeForms = ProcessDefinitionTest.class.getResourceAsStream("before-forms.xml").text
    private static final def afterProcessDef = ProcessDefinitionTest.class.getResourceAsStream("after-processdefwithform.xml").text
    private static final def afterForms = ProcessDefinitionTest.class.getResourceAsStream("after-forms.xml").text


    private Forms createForms() {
        def transientData = new ProcessDefinition(beforeProcessDef,true).getTransientData()
        def forms = new Forms(beforeForms, transientData, "6.3")
        return forms
    }
    @Test
    public void should_updateNameSpace_update_the_migrated_version() throws Exception {

        //when
        def forms = createForms()

        //then
        assertThat(forms.getContent()).contains('''<migration-product-version>6.3</migration-product-version>''')
    }

    @Test
    public void testUpdateExpressions() throws Exception {
        //given
        def forms = createForms()

        //when
        forms.updateExpressions();

        //then
        assertThat(forms.formsXml.depthFirst().findAll{it.name() == "expression-type" && it.text() == "TYPE_TRANSIENT_VARIABLE" }.size()).isEqualTo(1)
    }
    @Test
    public void testUpdateActions() throws Exception {
        //given
        def forms = createForms()

        //when
        forms.updateActions();

        //then
        assertThat(forms.formsXml.depthFirst().findAll{it.name() == "variable-type" && it.value() == "DOCUMENT" }.size()).isEqualTo(2)
        assertThat(forms.formsXml.depthFirst().findAll{it.name() == "action" && it.@type == "DOCUMENT_CREATE_UPDATE" }.size()).isEqualTo(0)
        assertThat(forms.formsXml.depthFirst().findAll{it.name() == "action" && it.@type == "EXECUTE_CONNECTOR" }.size()).isEqualTo(1)
        assertThat(forms.formsXml.depthFirst().findAll{it.name() == "variable-type" && it.value() == "DATA" }.size()).isEqualTo(3)
        assertThat(forms.formsXml.depthFirst().findAll{it.name() == "variable-type" && it.value() == "TRANSIENT_DATA" }.size()).isEqualTo(1)
        assertThat(forms.formsXml.depthFirst().findAll{it.name() == "is-external" }.size()).isEqualTo(0)
    }

    @Test
    public void should_getElementName_return_null_for_process() throws Exception {

        //when
        String elementName = Forms.getElementName("Pool--1.0\$entry");

        //then
        assertThat(elementName).isEmpty()
    }
    @Test
    public void should_getElementName_return_step_name_for_step() throws Exception {

        //when
        String elementName = Forms.getElementName("Pool--1.0--Step1\$entry");

        //then
        assertThat(elementName).isEqualTo("Step1")
    }


    @Test
    public void should_migrate_give_same_content() throws Exception {
        //given
        def processDefinition = new ProcessDefinition(beforeProcessDef,true)
        def transientData = processDefinition.getTransientData()
        def forms = new Forms(beforeForms, transientData, "6.3")


        //when

        transientData.each { processDefinition.updateExpressionOf(it) }
        processDefinition.updateOperatorAndLeftOperandType(transientData)
        forms.updateExpressions();
        forms.updateActions();

        //then
        def content = processDefinition.getContent()
        XMLUnit.setIgnoreWhitespace(true)
        def xmlDiff = new Diff(afterProcessDef, content)
        assert xmlDiff.identical()
        //check formatting did not change also
        assertThat(content.trim()).isEqualTo(afterProcessDef)


        content = forms.getContent()
        xmlDiff = new Diff(afterForms, content)
        assert xmlDiff.identical()
        //check formatting did not change also
        assertThat(content.trim()).isEqualTo(afterForms)
    }


}

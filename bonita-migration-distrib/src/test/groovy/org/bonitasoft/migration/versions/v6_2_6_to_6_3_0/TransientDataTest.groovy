package org.bonitasoft.migration.versions.v6_2_6_to_6_3_0

import static org.assertj.core.api.Assertions.*

import org.junit.Test


class TransientDataTest {

    @Test
    void testGetAttributes() {
        //when
        def trData = new TransientData(node:new XmlParser().parseText('''
          <textDataDefinition className="java.lang.String" longText="false" name="transientData" transient="true">
            <defaultValue expressionType="TYPE_CONSTANT" interpreter="NONE" name="constant" returnType="java.lang.String">
              <content>constant</content>
            </defaultValue>
          </textDataDefinition>
'''), containerId:12)
        //then
        assertThat(trData.haveInitialValue).isEqualTo(true)
        assertThat(trData.name).isEqualTo("transientData")
        assertThat(trData.containerId).isEqualTo(12)
    }
}

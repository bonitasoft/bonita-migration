<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="no" indent="yes" method="xml" encoding="UTF-8" standalone="yes" />
    <xsl:strip-space elements="*" />

    <!--
     reorder complex type child according to XSD
    -->
    <xsl:template match="*[local-name()='processDefinition']">
        <xsl:element name="{name()}" inherit-namespaces="no"
                     xpath-default-namespace="def"
                     namespace="http://www.bonitasoft.org/ns/process/client/7.4">
            <xsl:apply-templates select="@id" />
            <xsl:apply-templates select="@name" />
            <xsl:apply-templates select="@version" />
            <xsl:apply-templates select="description" />
            <xsl:apply-templates select="displayDescription" />
            <xsl:apply-templates select="parameters" />

            <!-- share new generated id between actor and actor initiator-->
            <xsl:variable name="initiatorId" select="translate(generate-id(),'N','_')" />

            <xsl:apply-templates select="actors">
                <xsl:with-param name="initiatorId" select="$initiatorId" />
            </xsl:apply-templates>
            <xsl:apply-templates select="actorInitiator">
                <xsl:with-param name="initiatorId" select="$initiatorId" />
            </xsl:apply-templates>

            <xsl:apply-templates select="flowElements" />
            <xsl:apply-templates select="stringIndexes" />
            <xsl:apply-templates select="contract" />
            <xsl:apply-templates select="context" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="flowElements">
        <xsl:element name="{name()}">

            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="automaticTask" />
            <xsl:apply-templates select="callActivity" />
            <xsl:apply-templates select="manualTask" />
            <xsl:apply-templates select="receiveTask" />
            <xsl:apply-templates select="sendTask" />
            <xsl:apply-templates select="userTask" />
            <xsl:apply-templates select="subProcess" />

            <xsl:apply-templates select="transitions" />
            <xsl:apply-templates select="gateway" />
            <xsl:apply-templates select="startEvent" />
            <xsl:apply-templates select="intermediateCatchEvent" />
            <xsl:apply-templates select="intermediateThrowEvent" />
            <xsl:apply-templates select="endEvent" />
            <xsl:apply-templates select="dataDefinitions" />

            <!-- fix wrong node name if needed -->
            <xsl:apply-templates select="BusinessDataDefinitions" />
            <xsl:apply-templates select="businessDataDefinitions" />

            <xsl:apply-templates select="documentDefinitions" />
            <xsl:apply-templates select="documentListDefinitions" />
            <xsl:apply-templates select="connectors" />
            <xsl:apply-templates select="elementFinder" />

        </xsl:element>
    </xsl:template>

    <xsl:template match="receiveTask">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:call-template name="commonActivityChildNamedTemplate" />

            <xsl:apply-templates select="catchMessageEventTrigger" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="sendTask">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:call-template name="commonActivityChildNamedTemplate" />

            <xsl:apply-templates select="throwMessageEventTrigger" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="contract">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="inputDefinitions" />
            <xsl:apply-templates select="constraints" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="throwMessageEventTrigger">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="correlation" />
            <xsl:apply-templates select="targetProcess" />
            <xsl:apply-templates select="targetFlowNode" />
            <xsl:apply-templates select="dataDefinition" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="correlation">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="value" />
            <xsl:apply-templates select="key" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="userTask">
        <xsl:call-template name="humanTaskNamedTemplate" />
    </xsl:template>

    <xsl:template match="manualTask">
        <xsl:call-template name="humanTaskNamedTemplate" />
    </xsl:template>

    <xsl:template match="automaticTask">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:call-template name="commonActivityChildNamedTemplate" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="callActivity">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:call-template name="commonActivityChildNamedTemplate" />

            <xsl:apply-templates select="callableElement" />
            <xsl:apply-templates select="callableElementVersion" />
            <xsl:apply-templates select="dataInputOperation" />
            <xsl:apply-templates select="contractInput" />
            <xsl:apply-templates select="dataOutputOperation" />

        </xsl:element>
    </xsl:template>

    <xsl:template match="startEvent">
        <xsl:call-template name="commonStartEventTemplate"/>
    </xsl:template>

    <xsl:template match="catchMessageEventTrigger">
        <xsl:element name="{name()}">

            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="correlation" />
            <xsl:apply-templates select="operation" />

        </xsl:element>
    </xsl:template>

    <xsl:template match="endEvent">
        <xsl:element name="{name()}">

            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="incomingTransition" />
            <xsl:apply-templates select="outgoingTransition" />
            <xsl:apply-templates select="connector" />
            <xsl:apply-templates select="description" />
            <xsl:apply-templates select="displayDescription" />
            <xsl:apply-templates select="displayName" />
            <xsl:apply-templates select="displayDescriptionAfterCompletion" />
            <xsl:apply-templates select="defaultTransition" />
            <xsl:apply-templates select="throwMessageEventTrigger" />
            <xsl:apply-templates select="throwSignalEventTrigger" />
            <xsl:apply-templates select="throwErrorEventTrigger" />
            <xsl:apply-templates select="terminateEventTrigger" />

        </xsl:element>
    </xsl:template>

    <xsl:template match="actors">
        <xsl:param name="initiatorId" />

        <xsl:element name="actors">
            <xsl:apply-templates select="actor">
                <xsl:with-param name="initiatorId" select="$initiatorId" />
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <xsl:template match="actor">
        <xsl:param name="initiatorId" />

        <xsl:variable name="currentName" select="@name" />

        <!--
        actor initiator is now identified by and xs:ID
        boolean field true if actor is actorInitiator
        -->
        <xsl:choose>
            <xsl:when test="count(preceding-sibling::actor[@name=$currentName]) &gt; 0">
                <!--
                BS-16079: exclude duplicate actor nodes with same name attribute since process definition
                may contains duplicates introduce by studio migration bug in 6.3

                already added actor node are skipped
                -->
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="actor">
                    <xsl:choose>
                        <xsl:when test="./@name=../../actorInitiator">
                            <xsl:attribute name="initiator">true</xsl:attribute>
                            <xsl:attribute name="id">
                                <xsl:value-of select="$initiatorId" />
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="initiator">false</xsl:attribute>
                            <xsl:attribute name="id">
                                <xsl:value-of select="translate(generate-id(),'N','_')" />
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:apply-templates select="@name" />
                    <xsl:apply-templates select="description" />
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- actor initiator is now a refId to actor @id -->
    <xsl:template match="actorInitiator">
        <xsl:param name="initiatorId" />

        <xsl:variable name="vActor" select="text()" />
        <xsl:if test="../actors/actor[@name=$vActor]">
            <xsl:element name="actorInitiator">
                <xsl:value-of select="$initiatorId" />
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="documentDefinition">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="url" />
            <xsl:apply-templates select="file" />
            <xsl:apply-templates select="description" />
            <xsl:apply-templates select="fileName" />
            <xsl:apply-templates select="expression" />

        </xsl:element>
    </xsl:template>

    <xsl:template match="inputDefinition">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <!--
            keep only first input definition
            duplicate may have been introduced in 7.2.0 step

            -->
            <xsl:apply-templates select="inputDefinitions[position()=1]" />
            <xsl:apply-templates select="description" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="BusinessDataDefinitions">
        <xsl:element name="businessDataDefinitions">
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>

    <xsl:template match="BusinessDataDefinition">
        <xsl:element name="businessDataDefinition">
            <xsl:call-template name="businessDataDefinitionNamedTemplate" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="businessDataDefinition">
        <xsl:element name="{name()}">
            <xsl:call-template name="businessDataDefinitionNamedTemplate" />
        </xsl:element>
    </xsl:template>

    <!-- generic expression nodes-->
    <xsl:template match="*[@expressionType]">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="content" />
            <xsl:apply-templates select="expression" />
        </xsl:element>
    </xsl:template>


    <xsl:template match="standardLoopCharacteristics">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="loopCondition" />
            <xsl:apply-templates select="loopMax" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="multiInstanceLoopCharacteristics">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="loopCardinality" />
            <xsl:apply-templates select="completionCondition" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="connectorDefinition">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="inputs" />
            <xsl:apply-templates select="outputs" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="operation">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="leftOperand" />
            <xsl:apply-templates select="rightOperand" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="boundaryEvent">
        <xsl:call-template name="commonStartEventTemplate" />
    </xsl:template>

    <xsl:template match="intermediateCatchEvent">
        <xsl:call-template name="commonStartEventTemplate" />
    </xsl:template>

    <xsl:template match="intermediateThrowEvent">
        <xsl:call-template name="commonThrowEventTemplate" />
    </xsl:template>
    <!--end of complex type reorder-->


    <!-- replace @expectedDuration attribute to expectedDuration expression node -->
    <xsl:template match="@expectedDuration">
        <xsl:element name="expectedDuration">
            <xsl:attribute name="expressionType">
                <xsl:text>TYPE_CONSTANT</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="id">
                <xsl:value-of select="translate(generate-id(),'N','_')" />
            </xsl:attribute>
            <xsl:attribute name="returnType">
                <xsl:text>java.lang.Long</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:text>expectedDuration expression</xsl:text>
            </xsl:attribute>
            <xsl:element name="content">
                <xsl:value-of select="." />
            </xsl:element>
        </xsl:element>
    </xsl:template>


    <!-- refId fields: make node compliant with target xs:ID-->

    <xsl:template match="outgoingTransition">
        <xsl:element name="{name()}">
            <xsl:value-of select="concat('_',.)" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="incomingTransition">
        <xsl:element name="{name()}">
            <xsl:value-of select="concat('_',.)" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="defaultTransition">
        <xsl:element name="{name()}">
            <xsl:value-of select="concat('_',.)" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="transition/@source">
        <xsl:attribute name="source">
            <xsl:value-of select="concat('_',.)" />
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="transition/@target">
        <xsl:attribute name="target">
            <xsl:value-of select="concat('_',.)" />
        </xsl:attribute>
    </xsl:template>

    <!-- end of refId transformation -->

    <!-- regenerate id for expression content -->
    <xsl:template match="@id[../@expressionType]">
        <xsl:attribute name="id">
            <xsl:value-of select="translate(generate-id(),'N','_')" />
        </xsl:attribute>
    </xsl:template>


    <!-- id attributes: make them compliant with xs:ID -->
    <xsl:template match="@id">
        <xsl:attribute name="id">
            <xsl:value-of select="concat('_',.)" />
        </xsl:attribute>
    </xsl:template>


    <!-- generic templates : no change are made -->

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{name()}" inherit-namespaces="yes">
            <xsl:apply-templates select="node()|@*" />
        </xsl:element>
    </xsl:template>

    <!-- name template called by apply templates above -->

    <xsl:template name="businessDataDefinitionNamedTemplate">
        <xsl:apply-templates select="@*" />
        <xsl:if test="not(@multiple)">
            <!-- attribute may be missing when migrating from 6.3 with bdm data definitions -->
            <xsl:attribute name="multiple">false</xsl:attribute>
        </xsl:if>

        <xsl:apply-templates select="description" />
        <xsl:apply-templates select="defaultValue" />
    </xsl:template>

    <xsl:template name="humanTaskNamedTemplate">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*[name()!='expectedDuration']" />

            <xsl:call-template name="commonActivityChildNamedTemplate" />
            <xsl:apply-templates select="userFilter" />
            <xsl:apply-templates select="contract" />
            <xsl:apply-templates select="context" />

            <!-- migrate attribute to expression child node -->
            <xsl:apply-templates select="@expectedDuration" />
        </xsl:element>
    </xsl:template>

    <xsl:template name="commonActivityChildNamedTemplate">
        <xsl:apply-templates select="incomingTransition" />
        <xsl:apply-templates select="outgoingTransition" />
        <xsl:apply-templates select="connector" />
        <xsl:apply-templates select="description" />
        <xsl:apply-templates select="displayDescription" />
        <xsl:apply-templates select="displayName" />
        <xsl:apply-templates select="displayDescriptionAfterCompletion" />
        <xsl:apply-templates select="defaultTransition" />
        <xsl:apply-templates select="dataDefinitions" />

        <!-- fix wrong node name if needed -->
        <xsl:apply-templates select="BusinessDataDefinitions" />
        <xsl:apply-templates select="businessDataDefinitions" />

        <xsl:apply-templates select="operations" />
        <xsl:apply-templates select="standardLoopCharacteristics" />
        <xsl:apply-templates select="multiInstanceLoopCharacteristics" />
        <xsl:apply-templates select="boundaryEvents" />
    </xsl:template>

    <xsl:template name="commonStartEventTemplate">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="incomingTransition" />
            <xsl:apply-templates select="outgoingTransition" />
            <xsl:apply-templates select="connector" />
            <xsl:apply-templates select="description" />
            <xsl:apply-templates select="displayDescription" />
            <xsl:apply-templates select="displayName" />
            <xsl:apply-templates select="displayDescriptionAfterCompletion" />
            <xsl:apply-templates select="defaultTransition" />
            <xsl:apply-templates select="timerEventTrigger" />
            <xsl:apply-templates select="catchMessageEventTrigger" />
            <xsl:apply-templates select="catchSignalEventTrigger" />
            <xsl:apply-templates select="catchErrorEventTrigger" />
        </xsl:element>
    </xsl:template>

    <xsl:template name="commonThrowEventTemplate">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" />

            <xsl:apply-templates select="incomingTransition" />
            <xsl:apply-templates select="outgoingTransition" />
            <xsl:apply-templates select="connector" />
            <xsl:apply-templates select="description" />
            <xsl:apply-templates select="displayDescription" />
            <xsl:apply-templates select="displayName" />
            <xsl:apply-templates select="displayDescriptionAfterCompletion" />
            <xsl:apply-templates select="defaultTransition" />
            <xsl:apply-templates select="throwMessageEventTrigger" />
            <xsl:apply-templates select="throwSignalEventTrigger" />
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>

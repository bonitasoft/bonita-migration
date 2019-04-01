package org.bonitasoft.migration.version.to7_9_0

import com.github.zafarkhaja.semver.Version

/**
 * @author Danila Mazour
 */
class ConnectorImplementation {

    String fileName
    String id
    String definitionId
    String version
    List<String> jarDependencies
    long resourceId

    static ConnectorImplementation parseConnectorXml(String content) {
        ConnectorImplementation connectorImplementation = new ConnectorImplementation()
        def xmlParser = new XmlParser().parseText(content)
        connectorImplementation.definitionId = xmlParser.definitionId.text()
        connectorImplementation.id = xmlParser.implementationId.text()
        connectorImplementation.version = xmlParser.implementationVersion.text()
        connectorImplementation.jarDependencies = xmlParser.jarDependencies.jarDependency.collect{it.text()}
        connectorImplementation
    }

    Version getSemanticVersion() {
        Version.valueOf(version)
    }


}

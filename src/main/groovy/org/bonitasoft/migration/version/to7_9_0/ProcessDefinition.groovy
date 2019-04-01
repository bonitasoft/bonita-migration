package org.bonitasoft.migration.version.to7_9_0

class ProcessDefinition {
    long tenantId
    long id
    String name
    String version

    ProcessDefinition(def tenantId, def id, def name, def version) {
        this.id = id
        this.tenantId = tenantId
        this.name = name
        this.version = version
    }
}

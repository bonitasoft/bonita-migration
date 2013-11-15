package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

public class TransitionInstance {

    def tenantid;

    def id;

    def rootContainerId;

    def parentContainerId;

    def name;

    def source;

    def processDefId;

    def tokenRefId;

    public TransitionInstance(final String tenantid, final String id , final String rootContainerId,final String parentContainerId,final String name,final String source,final String processDefId,final String tokenRefId){
        this.tenantid = tenantid
        this.id = id
        this.rootContainerId = rootContainerId
        this.parentContainerId = parentContainerId
        this.name = name
        this.source = source
        this.processDefId = processDefId
        this.tokenRefId = tokenRefId
    }
}

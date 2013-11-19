package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString
public class TransitionInstance {

    def tenantid;

    def id;

    def rootContainerId;

    def parentContainerId;

    def name;

    def source;

    def processDefId;

    def tokenRefId;
}

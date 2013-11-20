package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@EqualsAndHashCode
@ToString
public class GatewayInstance {
    def tenantid
    def id
    def flownodeDefinitionId
    def kind
    def rootContainerId
    def parentContainerId
    def name
    def stateId
    def prev_state_id
    def stateName
    def terminal
    def stable
    def stateCategory
    def gatewayType
    def hitBys
    def logicalGroup1
    def logicalGroup2
    def logicalGroup3
    def logicalGroup4
    def tokenCount
    def token_ref_id
}

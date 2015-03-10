--
-- FlowNode_instance
-- 
ALTER TABLE flownode_instance MODIFY kind VARCHAR2(25 CHAR) @@
ALTER TABLE flownode_instance MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE flownode_instance MODIFY displayName VARCHAR2(75 CHAR) @@
ALTER TABLE flownode_instance MODIFY displayDescription VARCHAR2(255 CHAR) @@
ALTER TABLE flownode_instance MODIFY stateName VARCHAR2(50 CHAR) @@
ALTER TABLE flownode_instance MODIFY gatewayType VARCHAR2(50 CHAR) @@
ALTER TABLE flownode_instance MODIFY hitBys VARCHAR2(255 CHAR) @@
ALTER TABLE flownode_instance MODIFY stateCategory VARCHAR2(50 CHAR) @@
ALTER TABLE flownode_instance MODIFY description VARCHAR2(255 CHAR) @@
ALTER TABLE flownode_instance MODIFY loopDataInputRef VARCHAR2(255 CHAR) @@
ALTER TABLE flownode_instance MODIFY loopDataOutputRef VARCHAR2(255 CHAR) @@
ALTER TABLE flownode_instance MODIFY dataInputItemRef VARCHAR2(255 CHAR) @@
ALTER TABLE flownode_instance MODIFY dataOutputItemRef VARCHAR2(255 CHAR) @@


--
-- Archived_flowNode_instance
-- 
ALTER TABLE arch_flownode_instance MODIFY kind VARCHAR2(25 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY displayName VARCHAR2(75 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY displayDescription VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY stateName VARCHAR2(50 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY gatewayType VARCHAR2(50 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY hitBys VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY description VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY loopDataInputRef VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY loopDataOutputRef VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY dataInputItemRef VARCHAR2(255 CHAR) @@
ALTER TABLE arch_flownode_instance MODIFY dataOutputItemRef VARCHAR2(255 CHAR) @@

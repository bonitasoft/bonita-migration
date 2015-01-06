--
-- FlowNode_instance
-- 

ALTER TABLE flownode_instance ADD kind_temp VARCHAR2(25 CHAR) @@
UPDATE flownode_instance SET kind_temp = kind @@
ALTER TABLE flownode_instance DROP COLUMN kind @@
ALTER TABLE flownode_instance RENAME COLUMN kind_temp TO kind @@
ALTER TABLE flownode_instance MODIFY kind NOT NULL @@


ALTER TABLE flownode_instance ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE flownode_instance SET name_temp = name @@
ALTER TABLE flownode_instance DROP COLUMN name @@
ALTER TABLE flownode_instance RENAME COLUMN name_temp TO name @@
ALTER TABLE flownode_instance MODIFY name NOT NULL @@


ALTER TABLE flownode_instance ADD displayName_temp VARCHAR2(75 CHAR) @@
UPDATE flownode_instance SET displayName_temp = displayName @@
ALTER TABLE flownode_instance DROP COLUMN displayName @@
ALTER TABLE flownode_instance RENAME COLUMN displayName_temp TO displayName @@


ALTER TABLE flownode_instance ADD displayDescription_temp VARCHAR2(255 CHAR) @@
UPDATE flownode_instance SET displayDescription_temp = displayDescription @@
ALTER TABLE flownode_instance DROP COLUMN displayDescription @@
ALTER TABLE flownode_instance RENAME COLUMN displayDescription_temp TO displayDescription @@


ALTER TABLE flownode_instance ADD stateName_temp VARCHAR2(50 CHAR) @@
UPDATE flownode_instance SET stateName_temp = stateName @@
ALTER TABLE flownode_instance DROP COLUMN stateName @@
ALTER TABLE flownode_instance RENAME COLUMN stateName_temp TO stateName @@


ALTER TABLE flownode_instance ADD gatewayType_temp VARCHAR2(50 CHAR) @@
UPDATE flownode_instance SET gatewayType_temp = gatewayType @@
ALTER TABLE flownode_instance DROP COLUMN gatewayType @@
ALTER TABLE flownode_instance RENAME COLUMN gatewayType_temp TO gatewayType @@


ALTER TABLE flownode_instance ADD hitBys_temp VARCHAR2(255 CHAR) @@
UPDATE flownode_instance SET hitBys_temp = hitBys @@
ALTER TABLE flownode_instance DROP COLUMN hitBys @@
ALTER TABLE flownode_instance RENAME COLUMN hitBys_temp TO hitBys @@


ALTER TABLE flownode_instance ADD stateCategory_temp VARCHAR2(50 CHAR) @@
UPDATE flownode_instance SET stateCategory_temp = stateCategory @@
ALTER TABLE flownode_instance DROP COLUMN stateCategory @@
ALTER TABLE flownode_instance RENAME COLUMN stateCategory_temp TO stateCategory @@
ALTER TABLE flownode_instance MODIFY stateCategory NOT NULL @@


ALTER TABLE flownode_instance ADD description_temp VARCHAR2(255 CHAR) @@
UPDATE flownode_instance SET description_temp = description @@
ALTER TABLE flownode_instance DROP COLUMN description @@
ALTER TABLE flownode_instance RENAME COLUMN description_temp TO description @@


ALTER TABLE flownode_instance ADD loopDataInputRef_temp VARCHAR2(255 CHAR) @@
UPDATE flownode_instance SET loopDataInputRef_temp = loopDataInputRef @@
ALTER TABLE flownode_instance DROP COLUMN loopDataInputRef @@
ALTER TABLE flownode_instance RENAME COLUMN loopDataInputRef_temp TO loopDataInputRef @@


ALTER TABLE flownode_instance ADD loopDataOutputRef_temp VARCHAR2(255 CHAR) @@
UPDATE flownode_instance SET loopDataOutputRef_temp = loopDataOutputRef @@
ALTER TABLE flownode_instance DROP COLUMN loopDataOutputRef @@
ALTER TABLE flownode_instance RENAME COLUMN loopDataOutputRef_temp TO loopDataOutputRef @@


ALTER TABLE flownode_instance ADD dataInputItemRef_temp VARCHAR2(255 CHAR) @@
UPDATE flownode_instance SET dataInputItemRef_temp = dataInputItemRef @@
ALTER TABLE flownode_instance DROP COLUMN dataInputItemRef @@
ALTER TABLE flownode_instance RENAME COLUMN dataInputItemRef_temp TO dataInputItemRef @@


ALTER TABLE flownode_instance ADD dataOutputItemRef_temp VARCHAR2(255 CHAR) @@
UPDATE flownode_instance SET dataOutputItemRef_temp = dataOutputItemRef @@
ALTER TABLE flownode_instance DROP COLUMN dataOutputItemRef @@
ALTER TABLE flownode_instance RENAME COLUMN dataOutputItemRef_temp TO dataOutputItemRef @@




--
-- Archived_flowNode_instance
-- 

ALTER TABLE arch_flownode_instance ADD kind_temp VARCHAR2(25 CHAR) @@
UPDATE arch_flownode_instance SET kind_temp = kind @@
ALTER TABLE arch_flownode_instance DROP COLUMN kind @@
ALTER TABLE arch_flownode_instance RENAME COLUMN kind_temp TO kind @@
ALTER TABLE arch_flownode_instance MODIFY kind NOT NULL @@


ALTER TABLE arch_flownode_instance ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE arch_flownode_instance SET name_temp = name @@
ALTER TABLE arch_flownode_instance DROP COLUMN name @@
ALTER TABLE arch_flownode_instance RENAME COLUMN name_temp TO name @@
ALTER TABLE arch_flownode_instance MODIFY name NOT NULL @@


ALTER TABLE arch_flownode_instance ADD displayName_temp VARCHAR2(75 CHAR) @@
UPDATE arch_flownode_instance SET displayName_temp = displayName @@
ALTER TABLE arch_flownode_instance DROP COLUMN displayName @@
ALTER TABLE arch_flownode_instance RENAME COLUMN displayName_temp TO displayName @@


ALTER TABLE arch_flownode_instance ADD displayDescription_temp VARCHAR2(255 CHAR) @@
UPDATE arch_flownode_instance SET displayDescription_temp = displayDescription @@
ALTER TABLE arch_flownode_instance DROP COLUMN displayDescription @@
ALTER TABLE arch_flownode_instance RENAME COLUMN displayDescription_temp TO displayDescription @@


ALTER TABLE arch_flownode_instance ADD stateName_temp VARCHAR2(50 CHAR) @@
UPDATE arch_flownode_instance SET stateName_temp = stateName @@
ALTER TABLE arch_flownode_instance DROP COLUMN stateName @@
ALTER TABLE arch_flownode_instance RENAME COLUMN stateName_temp TO stateName @@


ALTER TABLE arch_flownode_instance ADD gatewayType_temp VARCHAR2(50 CHAR) @@
UPDATE arch_flownode_instance SET gatewayType_temp = gatewayType @@
ALTER TABLE arch_flownode_instance DROP COLUMN gatewayType @@
ALTER TABLE arch_flownode_instance RENAME COLUMN gatewayType_temp TO gatewayType @@


ALTER TABLE arch_flownode_instance ADD hitBys_temp VARCHAR2(255 CHAR) @@
UPDATE arch_flownode_instance SET hitBys_temp = hitBys @@
ALTER TABLE arch_flownode_instance DROP COLUMN hitBys @@
ALTER TABLE arch_flownode_instance RENAME COLUMN hitBys_temp TO hitBys @@


ALTER TABLE arch_flownode_instance ADD description_temp VARCHAR2(255 CHAR) @@
UPDATE arch_flownode_instance SET description_temp = description @@
ALTER TABLE arch_flownode_instance DROP COLUMN description @@
ALTER TABLE arch_flownode_instance RENAME COLUMN description_temp TO description @@


ALTER TABLE arch_flownode_instance ADD loopDataInputRef_temp VARCHAR2(255 CHAR) @@
UPDATE arch_flownode_instance SET loopDataInputRef_temp = loopDataInputRef @@
ALTER TABLE arch_flownode_instance DROP COLUMN loopDataInputRef @@
ALTER TABLE arch_flownode_instance RENAME COLUMN loopDataInputRef_temp TO loopDataInputRef @@


ALTER TABLE arch_flownode_instance ADD loopDataOutputRef_temp VARCHAR2(255 CHAR) @@
UPDATE arch_flownode_instance SET loopDataOutputRef_temp = loopDataOutputRef @@
ALTER TABLE arch_flownode_instance DROP COLUMN loopDataOutputRef @@
ALTER TABLE arch_flownode_instance RENAME COLUMN loopDataOutputRef_temp TO loopDataOutputRef @@


ALTER TABLE arch_flownode_instance ADD dataInputItemRef_temp VARCHAR2(255 CHAR) @@
UPDATE arch_flownode_instance SET dataInputItemRef_temp = dataInputItemRef @@
ALTER TABLE arch_flownode_instance DROP COLUMN dataInputItemRef @@
ALTER TABLE arch_flownode_instance RENAME COLUMN dataInputItemRef_temp TO dataInputItemRef @@


ALTER TABLE arch_flownode_instance ADD dataOutputItemRef_temp VARCHAR2(255 CHAR) @@
UPDATE arch_flownode_instance SET dataOutputItemRef_temp = dataOutputItemRef @@
ALTER TABLE arch_flownode_instance DROP COLUMN dataOutputItemRef @@
ALTER TABLE arch_flownode_instance RENAME COLUMN dataOutputItemRef_temp TO dataOutputItemRef @@
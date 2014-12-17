--
-- Message_instance
-- 

ALTER TABLE message_instance ADD messageName_temp VARCHAR2(255 CHAR) @@
UPDATE message_instance SET messageName_temp = messageName @@
ALTER TABLE message_instance DROP COLUMN messageName @@
ALTER TABLE message_instance RENAME COLUMN messageName_temp TO messageName @@
ALTER TABLE message_instance MODIFY messageName NOT NULL @@


ALTER TABLE message_instance ADD targetProcess_temp VARCHAR2(255 CHAR) @@
UPDATE message_instance SET targetProcess_temp = targetProcess @@
ALTER TABLE message_instance DROP COLUMN targetProcess @@
ALTER TABLE message_instance RENAME COLUMN targetProcess_temp TO targetProcess @@
ALTER TABLE message_instance MODIFY targetProcess NOT NULL @@


ALTER TABLE message_instance ADD targetFlowNode_temp VARCHAR2(255 CHAR) NULL @@
UPDATE message_instance SET targetFlowNode_temp = targetFlowNode @@
ALTER TABLE message_instance DROP COLUMN targetFlowNode @@
ALTER TABLE message_instance RENAME COLUMN targetFlowNode_temp TO targetFlowNode @@


ALTER TABLE message_instance ADD flowNodeName_temp VARCHAR2(255 CHAR) @@
UPDATE message_instance SET flowNodeName_temp = flowNodeName @@
ALTER TABLE message_instance DROP COLUMN flowNodeName @@
ALTER TABLE message_instance RENAME COLUMN flowNodeName_temp TO flowNodeName @@


ALTER TABLE message_instance ADD correlation1_temp VARCHAR2(128 CHAR) @@
UPDATE message_instance SET correlation1_temp = correlation1 @@
ALTER TABLE message_instance DROP COLUMN correlation1 @@
ALTER TABLE message_instance RENAME COLUMN correlation1_temp TO correlation1 @@


ALTER TABLE message_instance ADD correlation2_temp VARCHAR2(128 CHAR) @@
UPDATE message_instance SET correlation2_temp = correlation2 @@
ALTER TABLE message_instance DROP COLUMN correlation2 @@
ALTER TABLE message_instance RENAME COLUMN correlation2_temp TO correlation2 @@


ALTER TABLE message_instance ADD correlation3_temp VARCHAR2(128 CHAR) @@
UPDATE message_instance SET correlation3_temp = correlation3 @@
ALTER TABLE message_instance DROP COLUMN correlation3 @@
ALTER TABLE message_instance RENAME COLUMN correlation3_temp TO correlation3 @@


ALTER TABLE message_instance ADD correlation4_temp VARCHAR2(128 CHAR) @@
UPDATE message_instance SET correlation4_temp = correlation4 @@
ALTER TABLE message_instance DROP COLUMN correlation4 @@
ALTER TABLE message_instance RENAME COLUMN correlation4_temp TO correlation4 @@


ALTER TABLE message_instance ADD correlation5_temp VARCHAR2(128 CHAR) @@
UPDATE message_instance SET correlation5_temp = correlation5 @@
ALTER TABLE message_instance DROP COLUMN correlation5 @@
ALTER TABLE message_instance RENAME COLUMN correlation5_temp TO correlation5 @@
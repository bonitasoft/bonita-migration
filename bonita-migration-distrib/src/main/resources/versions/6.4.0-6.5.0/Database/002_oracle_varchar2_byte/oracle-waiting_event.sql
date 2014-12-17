--
-- Waiting_event
-- 

ALTER TABLE waiting_event ADD kind_temp VARCHAR2(15 CHAR) @@
UPDATE waiting_event SET kind_temp = kind @@
ALTER TABLE waiting_event DROP COLUMN kind @@
ALTER TABLE waiting_event RENAME COLUMN kind_temp TO kind @@
ALTER TABLE waiting_event MODIFY kind NOT NULL @@


ALTER TABLE waiting_event ADD eventType_temp VARCHAR2(50 CHAR) @@
UPDATE waiting_event SET eventType_temp = eventType @@
ALTER TABLE waiting_event DROP COLUMN eventType @@
ALTER TABLE waiting_event RENAME COLUMN eventType_temp TO eventType @@


ALTER TABLE waiting_event ADD messageName_temp VARCHAR2(255 CHAR) @@
UPDATE waiting_event SET messageName_temp = messageName @@
ALTER TABLE waiting_event DROP COLUMN messageName @@
ALTER TABLE waiting_event RENAME COLUMN messageName_temp TO messageName @@


ALTER TABLE waiting_event ADD signalName_temp VARCHAR2(255 CHAR) @@
UPDATE waiting_event SET signalName_temp = signalName @@
ALTER TABLE waiting_event DROP COLUMN signalName @@
ALTER TABLE waiting_event RENAME COLUMN signalName_temp TO signalName @@


ALTER TABLE waiting_event ADD errorCode_temp VARCHAR2(255 CHAR) @@
UPDATE waiting_event SET errorCode_temp = errorCode @@
ALTER TABLE waiting_event DROP COLUMN errorCode @@
ALTER TABLE waiting_event RENAME COLUMN errorCode_temp TO errorCode @@


ALTER TABLE waiting_event ADD processName_temp VARCHAR2(150 CHAR) @@
UPDATE waiting_event SET processName_temp = processName @@
ALTER TABLE waiting_event DROP COLUMN processName @@
ALTER TABLE waiting_event RENAME COLUMN processName_temp TO processName @@


ALTER TABLE waiting_event ADD flowNodeName_temp VARCHAR2(50 CHAR) @@
UPDATE waiting_event SET flowNodeName_temp = flowNodeName @@
ALTER TABLE waiting_event DROP COLUMN flowNodeName @@
ALTER TABLE waiting_event RENAME COLUMN flowNodeName_temp TO flowNodeName @@


ALTER TABLE waiting_event ADD correlation1_temp VARCHAR2(128 CHAR) @@
UPDATE waiting_event SET correlation1_temp = correlation1 @@
ALTER TABLE waiting_event DROP COLUMN correlation1 @@
ALTER TABLE waiting_event RENAME COLUMN correlation1_temp TO correlation1 @@


ALTER TABLE waiting_event ADD correlation2_temp VARCHAR2(128 CHAR) @@
UPDATE waiting_event SET correlation2_temp = correlation2 @@
ALTER TABLE waiting_event DROP COLUMN correlation2 @@
ALTER TABLE waiting_event RENAME COLUMN correlation2_temp TO correlation2 @@


ALTER TABLE waiting_event ADD correlation3_temp VARCHAR2(128 CHAR) @@
UPDATE waiting_event SET correlation3_temp = correlation3 @@
ALTER TABLE waiting_event DROP COLUMN correlation3 @@
ALTER TABLE waiting_event RENAME COLUMN correlation3_temp TO correlation3 @@


ALTER TABLE waiting_event ADD correlation4_temp VARCHAR2(128 CHAR) @@
UPDATE waiting_event SET correlation4_temp = correlation4 @@
ALTER TABLE waiting_event DROP COLUMN correlation4 @@
ALTER TABLE waiting_event RENAME COLUMN correlation4_temp TO correlation4 @@


ALTER TABLE waiting_event ADD correlation5_temp VARCHAR2(128 CHAR) @@
UPDATE waiting_event SET correlation5_temp = correlation5 @@
ALTER TABLE waiting_event DROP COLUMN correlation5 @@
ALTER TABLE waiting_event RENAME COLUMN correlation5_temp TO correlation5 @@
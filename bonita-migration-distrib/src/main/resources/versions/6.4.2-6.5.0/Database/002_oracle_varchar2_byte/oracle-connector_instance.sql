--
-- Connector_instance
-- 

ALTER TABLE connector_instance ADD containerType_temp VARCHAR2(10 CHAR) @@
UPDATE connector_instance SET containerType_temp = containerType @@
ALTER TABLE connector_instance DROP COLUMN containerType @@
ALTER TABLE connector_instance RENAME COLUMN containerType_temp TO containerType @@
ALTER TABLE connector_instance MODIFY containerType NOT NULL @@


ALTER TABLE connector_instance ADD connectorId_temp VARCHAR2(255 CHAR) @@
UPDATE connector_instance SET connectorId_temp = connectorId @@
ALTER TABLE connector_instance DROP COLUMN connectorId @@
ALTER TABLE connector_instance RENAME COLUMN connectorId_temp TO connectorId @@
ALTER TABLE connector_instance MODIFY connectorId NOT NULL @@


ALTER TABLE connector_instance ADD version_temp VARCHAR2(10 CHAR) @@
UPDATE connector_instance SET version_temp = version @@
ALTER TABLE connector_instance DROP COLUMN version @@
ALTER TABLE connector_instance RENAME COLUMN version_temp TO version @@
ALTER TABLE connector_instance MODIFY version NOT NULL @@


ALTER TABLE connector_instance ADD name_temp VARCHAR2(255 CHAR) @@
UPDATE connector_instance SET name_temp = name @@
ALTER TABLE connector_instance DROP COLUMN name @@
ALTER TABLE connector_instance RENAME COLUMN name_temp TO name @@
ALTER TABLE connector_instance MODIFY name NOT NULL @@


ALTER TABLE connector_instance ADD activationEvent_temp VARCHAR2(30 CHAR) @@
UPDATE connector_instance SET activationEvent_temp = activationEvent @@
ALTER TABLE connector_instance DROP COLUMN activationEvent @@
ALTER TABLE connector_instance RENAME COLUMN activationEvent_temp TO activationEvent @@


ALTER TABLE connector_instance ADD state_temp VARCHAR2(50 CHAR) @@
UPDATE connector_instance SET state_temp = state @@
ALTER TABLE connector_instance DROP COLUMN state @@
ALTER TABLE connector_instance RENAME COLUMN state_temp TO state @@


ALTER TABLE connector_instance ADD exceptionMessage_temp VARCHAR2(255 CHAR) @@
UPDATE connector_instance SET exceptionMessage_temp = exceptionMessage @@
ALTER TABLE connector_instance DROP COLUMN exceptionMessage @@
ALTER TABLE connector_instance RENAME COLUMN exceptionMessage_temp TO exceptionMessage @@



--
-- Archived_connector_instance
-- 

ALTER TABLE arch_connector_instance ADD containerType_temp VARCHAR2(10 CHAR) @@
UPDATE arch_connector_instance SET containerType_temp = containerType @@
ALTER TABLE arch_connector_instance DROP COLUMN containerType @@
ALTER TABLE arch_connector_instance RENAME COLUMN containerType_temp TO containerType @@
ALTER TABLE arch_connector_instance MODIFY containerType NOT NULL @@


ALTER TABLE arch_connector_instance ADD connectorId_temp VARCHAR2(255 CHAR) @@
UPDATE arch_connector_instance SET connectorId_temp = connectorId @@
ALTER TABLE arch_connector_instance DROP COLUMN connectorId @@
ALTER TABLE arch_connector_instance RENAME COLUMN connectorId_temp TO connectorId @@
ALTER TABLE arch_connector_instance MODIFY connectorId NOT NULL @@


ALTER TABLE arch_connector_instance ADD version_temp VARCHAR2(10 CHAR) @@
UPDATE arch_connector_instance SET version_temp = version @@
ALTER TABLE arch_connector_instance DROP COLUMN version @@
ALTER TABLE arch_connector_instance RENAME COLUMN version_temp TO version @@
ALTER TABLE arch_connector_instance MODIFY version NOT NULL @@


ALTER TABLE arch_connector_instance ADD name_temp VARCHAR2(255 CHAR) @@
UPDATE arch_connector_instance SET name_temp = name @@
ALTER TABLE arch_connector_instance DROP COLUMN name @@
ALTER TABLE arch_connector_instance RENAME COLUMN name_temp TO name @@
ALTER TABLE arch_connector_instance MODIFY name NOT NULL @@


ALTER TABLE arch_connector_instance ADD activationEvent_temp VARCHAR2(30 CHAR) @@
UPDATE arch_connector_instance SET activationEvent_temp = activationEvent @@
ALTER TABLE arch_connector_instance DROP COLUMN activationEvent @@
ALTER TABLE arch_connector_instance RENAME COLUMN activationEvent_temp TO activationEvent @@


ALTER TABLE arch_connector_instance ADD state_temp VARCHAR2(50 CHAR) @@
UPDATE arch_connector_instance SET state_temp = state @@
ALTER TABLE arch_connector_instance DROP COLUMN state @@
ALTER TABLE arch_connector_instance RENAME COLUMN state_temp TO state @@

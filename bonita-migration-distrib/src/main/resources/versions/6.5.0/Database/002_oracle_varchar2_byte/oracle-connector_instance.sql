--
-- Connector_instance
-- 

ALTER TABLE connector_instance MODIFY containerType VARCHAR2(10 CHAR) @@
ALTER TABLE connector_instance MODIFY connectorId VARCHAR2(255 CHAR) @@
ALTER TABLE connector_instance MODIFY version VARCHAR2(10 CHAR) @@
ALTER TABLE connector_instance MODIFY name VARCHAR2(255 CHAR) @@
ALTER TABLE connector_instance MODIFY activationEvent VARCHAR2(30 CHAR) @@
ALTER TABLE connector_instance MODIFY state VARCHAR2(50 CHAR) @@
ALTER TABLE connector_instance MODIFY exceptionMessage VARCHAR2(255 CHAR) @@


--
-- Archived_connector_instance
-- 

ALTER TABLE arch_connector_instance MODIFY containerType VARCHAR2(10 CHAR) @@
ALTER TABLE arch_connector_instance MODIFY connectorId VARCHAR2(255 CHAR) @@
ALTER TABLE arch_connector_instance MODIFY version VARCHAR2(10 CHAR) @@
ALTER TABLE arch_connector_instance MODIFY name VARCHAR2(255 CHAR) @@
ALTER TABLE arch_connector_instance MODIFY activationEvent VARCHAR2(30 CHAR) @@
ALTER TABLE arch_connector_instance MODIFY state VARCHAR2(50 CHAR) @@

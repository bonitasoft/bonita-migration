--
-- Waiting_event
-- 
ALTER TABLE waiting_event MODIFY kind VARCHAR2(15 CHAR) @@
ALTER TABLE waiting_event MODIFY eventType VARCHAR2(50 CHAR) @@
ALTER TABLE waiting_event MODIFY messageName VARCHAR2(255 CHAR) @@
ALTER TABLE waiting_event MODIFY signalName VARCHAR2(255 CHAR) @@
ALTER TABLE waiting_event MODIFY errorCode VARCHAR2(255 CHAR) @@
ALTER TABLE waiting_event MODIFY processName VARCHAR2(150 CHAR) @@
ALTER TABLE waiting_event MODIFY flowNodeName VARCHAR2(50 CHAR) @@
ALTER TABLE waiting_event MODIFY correlation1 VARCHAR2(128 CHAR) @@
ALTER TABLE waiting_event MODIFY correlation2 VARCHAR2(128 CHAR) @@
ALTER TABLE waiting_event MODIFY correlation3 VARCHAR2(128 CHAR) @@
ALTER TABLE waiting_event MODIFY correlation4 VARCHAR2(128 CHAR) @@
ALTER TABLE waiting_event MODIFY correlation5 VARCHAR2(128 CHAR) @@

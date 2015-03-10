--
-- Process_definition
-- 
ALTER TABLE process_definition MODIFY name VARCHAR2(150 CHAR) @@
ALTER TABLE process_definition MODIFY version VARCHAR2(50 CHAR) @@
ALTER TABLE process_definition MODIFY description VARCHAR2(255 CHAR) @@
ALTER TABLE process_definition MODIFY activationState VARCHAR2(30 CHAR) @@
ALTER TABLE process_definition MODIFY configurationState VARCHAR2(30 CHAR) @@
ALTER TABLE process_definition MODIFY displayName VARCHAR2(75 CHAR) @@
ALTER TABLE process_definition MODIFY displayDescription VARCHAR2(255 CHAR) @@
ALTER TABLE process_definition MODIFY iconPath VARCHAR2(255 CHAR) @@

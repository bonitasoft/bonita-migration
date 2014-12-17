--
-- Queriable_log
-- 

ALTER TABLE queriable_log ADD userId_temp VARCHAR2(255 CHAR) @@
UPDATE queriable_log SET userId_temp = userId @@
ALTER TABLE queriable_log DROP COLUMN userId @@
ALTER TABLE queriable_log RENAME COLUMN userId_temp TO userId @@
ALTER TABLE queriable_log MODIFY userId NOT NULL @@


ALTER TABLE queriable_log ADD clusterNode_temp VARCHAR2(50 CHAR) @@
UPDATE queriable_log SET clusterNode_temp = clusterNode @@
ALTER TABLE queriable_log DROP COLUMN clusterNode @@
ALTER TABLE queriable_log RENAME COLUMN clusterNode_temp TO clusterNode @@


ALTER TABLE queriable_log ADD productVersion_temp VARCHAR2(50 CHAR) @@
UPDATE queriable_log SET productVersion_temp = productVersion @@
ALTER TABLE queriable_log DROP COLUMN productVersion @@
ALTER TABLE queriable_log RENAME COLUMN productVersion_temp TO productVersion @@
ALTER TABLE queriable_log MODIFY productVersion NOT NULL @@


ALTER TABLE queriable_log ADD severity_temp VARCHAR2(50 CHAR) @@
UPDATE queriable_log SET severity_temp = severity @@
ALTER TABLE queriable_log DROP COLUMN severity @@
ALTER TABLE queriable_log RENAME COLUMN severity_temp TO severity @@
ALTER TABLE queriable_log MODIFY severity NOT NULL @@


ALTER TABLE queriable_log ADD actionType_temp VARCHAR2(50 CHAR) @@
UPDATE queriable_log SET actionType_temp = actionType @@
ALTER TABLE queriable_log DROP COLUMN actionType @@
ALTER TABLE queriable_log RENAME COLUMN actionType_temp TO actionType @@
ALTER TABLE queriable_log MODIFY actionType NOT NULL @@


ALTER TABLE queriable_log ADD actionScope_temp VARCHAR2(100 CHAR) @@
UPDATE queriable_log SET actionScope_temp = actionScope @@
ALTER TABLE queriable_log DROP COLUMN actionScope @@
ALTER TABLE queriable_log RENAME COLUMN actionScope_temp TO actionScope @@


ALTER TABLE queriable_log ADD rawMessage_temp VARCHAR2(255 CHAR) @@
UPDATE queriable_log SET rawMessage_temp = rawMessage @@
ALTER TABLE queriable_log DROP COLUMN rawMessage @@
ALTER TABLE queriable_log RENAME COLUMN rawMessage_temp TO rawMessage @@
ALTER TABLE queriable_log MODIFY rawMessage NOT NULL @@


ALTER TABLE queriable_log ADD callerClassName_temp VARCHAR2(200 CHAR) @@
UPDATE queriable_log SET callerClassName_temp = callerClassName @@
ALTER TABLE queriable_log DROP COLUMN callerClassName @@
ALTER TABLE queriable_log RENAME COLUMN callerClassName_temp TO callerClassName @@


ALTER TABLE queriable_log ADD callerMethodName_temp VARCHAR2(80 CHAR) @@
UPDATE queriable_log SET callerMethodName_temp = callerMethodName @@
ALTER TABLE queriable_log DROP COLUMN callerMethodName @@
ALTER TABLE queriable_log RENAME COLUMN callerMethodName_temp TO callerMethodName @@



--
-- Queriable_log_p 
-- 

ALTER TABLE queriablelog_p ADD name_temp VARCHAR2(50 CHAR) @@
UPDATE queriablelog_p SET name_temp = name @@
ALTER TABLE queriablelog_p DROP COLUMN name @@
ALTER TABLE queriablelog_p RENAME COLUMN name_temp TO name @@
ALTER TABLE queriablelog_p MODIFY name NOT NULL @@


ALTER TABLE queriablelog_p ADD stringValue_temp VARCHAR2(255 CHAR) @@
UPDATE queriablelog_p SET stringValue_temp = stringValue @@
ALTER TABLE queriablelog_p DROP COLUMN stringValue @@
ALTER TABLE queriablelog_p RENAME COLUMN stringValue_temp TO stringValue @@


ALTER TABLE queriablelog_p ADD valueType_temp VARCHAR2(30 CHAR) @@
UPDATE queriablelog_p SET valueType_temp = valueType @@
ALTER TABLE queriablelog_p DROP COLUMN valueType @@
ALTER TABLE queriablelog_p RENAME COLUMN valueType_temp TO valueType @@


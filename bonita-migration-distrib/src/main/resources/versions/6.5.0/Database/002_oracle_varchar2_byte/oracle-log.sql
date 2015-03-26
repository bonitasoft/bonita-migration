--
-- Queriable_log
-- 
ALTER TABLE queriable_log MODIFY userId VARCHAR2(255 CHAR) @@
ALTER TABLE queriable_log MODIFY clusterNode VARCHAR2(50 CHAR) @@
ALTER TABLE queriable_log MODIFY productVersion VARCHAR2(50 CHAR) @@
ALTER TABLE queriable_log MODIFY severity VARCHAR2(50 CHAR) @@
ALTER TABLE queriable_log MODIFY actionType VARCHAR2(50 CHAR) @@
ALTER TABLE queriable_log MODIFY actionScope VARCHAR2(100 CHAR) @@
ALTER TABLE queriable_log MODIFY rawMessage VARCHAR2(255 CHAR) @@
ALTER TABLE queriable_log MODIFY callerClassName VARCHAR2(200 CHAR) @@
ALTER TABLE queriable_log MODIFY callerMethodName VARCHAR2(80 CHAR) @@


--
-- Queriable_log_p 
-- 
ALTER TABLE queriablelog_p MODIFY name VARCHAR2(50 CHAR) @@
ALTER TABLE queriablelog_p MODIFY stringValue VARCHAR2(255 CHAR) @@
ALTER TABLE queriablelog_p MODIFY valueType VARCHAR2(30 CHAR) @@

ALTER TABLE queriable_log CHANGE year whatYear SMALLINT
@@
ALTER TABLE queriable_log CHANGE month whatMonth TINYINT
@@
ALTER TABLE queriable_log CHANGE timeStamp log_timestamp BIGINT NOT NULL

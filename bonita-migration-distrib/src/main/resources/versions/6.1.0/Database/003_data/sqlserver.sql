--
-- datasource
-- 

DROP INDEX idx_datasource_name ON datasource
@@
CREATE INDEX idx_datasource_name ON datasource (name ASC, id ASC)
@@
DROP INDEX idx_datasource_version ON datasource
@@
CREATE INDEX idx_datasource_version ON datasource (version ASC, id ASC)
@@

--
-- datasourceparameter
-- 

DROP INDEX idx_datasourceparameter_datasourceid ON datasourceparameter
@@
CREATE INDEX idx_datasourceparameter_datasourceid ON datasourceparameter (datasourceId ASC, id ASC)
@@
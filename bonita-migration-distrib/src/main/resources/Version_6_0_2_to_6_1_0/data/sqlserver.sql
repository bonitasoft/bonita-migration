--
-- datasource
-- 

DROP INDEX idx_datasource_name ON datasource
GO
CREATE INDEX idx_datasource_name ON datasource (name ASC, id ASC)
GO
DROP INDEX idx_datasource_version ON datasource
GO
CREATE INDEX idx_datasource_version ON datasource (version ASC, id ASC)
GO

--
-- datasourceparameter
-- 

DROP INDEX idx_datasourceparameter_datasourceid ON datasourceparameter
GO
CREATE INDEX idx_datasourceparameter_datasourceid ON datasourceparameter (datasourceId ASC, id ASC)
GO
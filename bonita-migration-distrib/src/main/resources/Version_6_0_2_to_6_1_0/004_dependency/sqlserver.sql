--
-- dependency
-- 

DROP INDEX idx_dependency_name ON dependency
GO
CREATE INDEX idx_dependency_name ON dependency (name ASC, id ASC)
GO
DROP INDEX idx_dependency_version ON dependency
GO
CREATE INDEX idx_dependency_version ON dependency (version ASC, id ASC)
GO

--
-- dependencymapping
-- 

DROP INDEX idx_dependencymapping_depid ON dependencymapping
GO
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid ASC, id ASC)
GO


--
-- pdependency
-- 

DROP INDEX idx_pdependency_name ON pdependency
GO
CREATE INDEX idx_pdependency_name ON pdependency (name ASC, id ASC)
GO
DROP INDEX idx_pdependency_version ON pdependency
GO
CREATE INDEX idx_pdependency_version ON pdependency (version ASC, id ASC)
GO


--
-- pdependencymapping
-- 

DROP INDEX idx_pdependencymapping_depid ON pdependencymapping
GO
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid ASC, id ASC)
GO
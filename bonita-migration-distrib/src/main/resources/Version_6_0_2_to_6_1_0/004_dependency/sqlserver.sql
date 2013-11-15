--
-- dependency
-- 

DROP INDEX idx_dependency_name ON dependency
@@
CREATE INDEX idx_dependency_name ON dependency (name ASC, id ASC)
@@
DROP INDEX idx_dependency_version ON dependency
@@
CREATE INDEX idx_dependency_version ON dependency (version ASC, id ASC)
@@

--
-- dependencymapping
-- 

DROP INDEX idx_dependencymapping_depid ON dependencymapping
@@
CREATE INDEX idx_dependencymapping_depid ON dependencymapping (dependencyid ASC, id ASC)
@@


--
-- pdependency
-- 

DROP INDEX idx_pdependency_name ON pdependency
@@
CREATE INDEX idx_pdependency_name ON pdependency (name ASC, id ASC)
@@
DROP INDEX idx_pdependency_version ON pdependency
@@
CREATE INDEX idx_pdependency_version ON pdependency (version ASC, id ASC)
@@


--
-- pdependencymapping
-- 

DROP INDEX idx_pdependencymapping_depid ON pdependencymapping
@@
CREATE INDEX idx_pdependencymapping_depid ON pdependencymapping (dependencyid ASC, id ASC)
@@
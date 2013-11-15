--
-- p_metadata_def
-- 

DROP INDEX idx_p_metadata_def_name ON p_metadata_def
@@
CREATE INDEX idx_p_metadata_def_name ON p_metadata_def (name ASC, id ASC)
@@


--
-- role
-- 

DROP INDEX idx_role_name ON role
@@
CREATE INDEX idx_role_name ON role (tenantid ASC, name ASC, id ASC)
@@


--
-- user_
-- 

DROP INDEX idx_user_name ON user_
@@
CREATE INDEX idx_user_name ON user_ (tenantid ASC, userName ASC, id ASC)
@@
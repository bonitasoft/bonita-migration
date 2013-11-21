--
-- profile
-- 

ALTER TABLE profile ADD isDefault TINYINT(1) NOT NULL;
ALTER TABLE profile ADD creationDate BIGINT(20) NOT NULL;
ALTER TABLE profile ADD createdBy BIGINT(20) NOT NULL;
ALTER TABLE profile ADD lastUpdateDate BIGINT(20) NOT NULL;
ALTER TABLE profile ADD lastUpdatedBy BIGINT(20) NOT NULL;


--
-- profileentry
-- 

ALTER TABLE profileentry MODIFY COLUMN name VARCHAR(50) NULL;
CREATE INDEX indexProfileEntry ON profileentry (tenantId ASC, parentId ASC, profileId ASC);
ALTER TABLE profileentry DROP INDEX tenantId;


--
-- Datas
--

UPDATE profile 
	SET creationDate = ?,
	createdBy = -1, 
	lastUpdateDate = ?,
	lastUpdatedBy = -1,
	isDefault = 1
	WHERE name in ('User', 'Administrator');
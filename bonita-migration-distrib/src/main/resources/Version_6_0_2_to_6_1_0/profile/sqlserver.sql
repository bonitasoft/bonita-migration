--
-- profile
-- 

ALTER TABLE profile ADD isDefault BIT NOT NULL
GO
ALTER TABLE profile ADD creationDate NUMERIC(19,0) NOT NULL
GO
ALTER TABLE profile ADD createdBy NUMERIC(19,0) NOT NULL
GO
ALTER TABLE profile ADD lastUpdateDate NUMERIC(19,0) NOT NULL
GO
ALTER TABLE profile ADD lastUpdatedBy NUMERIC(19,0) NOT NULL
GO

--
-- profileentry
-- 

DECLARE @profileentry_ukey_name nvarchar(255), @profileentry_alter_table_sql VARCHAR(4000)
SET @profileentry_alter_table_sql = 'ALTER TABLE profileentry DROP CONSTRAINT |ConstraintName| '

SELECT @profileentry_ukey_name = name FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('profileentry')
AND type = 'UQ'

IF not @profileentry_ukey_name IS NULL
BEGIN
	SET @profileentry_alter_table_sql = REPLACE(@profileentry_alter_table_sql, '|ConstraintName|', @profileentry_ukey_name)
	EXEC (@profileentry_alter_table_sql)
END

ALTER TABLE profileentry ALTER COLUMN name NVARCHAR(50) NULL
GO
CREATE INDEX indexProfileEntry ON profileentry (tenantId ASC, parentId ASC, profileId ASC)
GO
   
                                                                    
--
-- Datas
--

UPDATE profile 
SET creationDate = :creationDate,
	createdBy = -1, 
	lastUpdateDate = :creationDate,
	lastUpdatedBy = -1,
	isDefault = TRUE
WHERE name in ('User', 'Administrator')
GO
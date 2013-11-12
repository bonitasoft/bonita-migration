--
-- FOREIGN KEYS [DROP]
-- 

ALTER TABLE profileentry DROP CONSTRAINT fk_profileentry_profileid;
ALTER TABLE profilemember DROP CONSTRAINT fk_profilemember_profileid;


--
-- profile
-- 

ALTER TABLE profile ADD isdefault BOOL NOT NULL DEFAULT FALSE;
ALTER TABLE profile ADD creationdate INT8 NOT NULL DEFAULT 0;
ALTER TABLE profile ADD createdby INT8 NOT NULL DEFAULT 0;
ALTER TABLE profile ADD lastupdatedate INT8 NOT NULL DEFAULT 0;
ALTER TABLE profile ADD lastupdatedby INT8 NOT NULL DEFAULT 0;


--
-- profileentry
-- 

ALTER TABLE profileentry ALTER COLUMN name DROP NOT NULL;
CREATE INDEX indexprofileentry ON profileentry (tenantid, parentid, profileid);
ALTER TABLE profileentry DROP CONSTRAINT profileentry_tenantid_parentid_profileid_name_key;


--
-- FOREIGN KEYS [CREATE]
-- 

ALTER TABLE profileentry ADD CONSTRAINT fk_profileentry_profileid FOREIGN KEY (tenantid,profileid) REFERENCES profile (tenantid, id)
                                                                  ON DELETE NO ACTION
                                                                  ON UPDATE NO ACTION;
ALTER TABLE profilemember ADD CONSTRAINT fk_profilemember_profileid FOREIGN KEY (tenantid,profileid) REFERENCES profile (tenantid, id)
                                                                    ON DELETE NO ACTION
                                                                    ON UPDATE NO ACTION;
                                                                    
--
-- Datas
--

UPDATE profile 
	SET creationDate = :creationDate,
	createdBy = -1, 
	lastUpdateDate = :creationDate,
	lastUpdatedBy = -1,
	isDefault = TRUE
	WHERE name in ('User', 'Administrator');

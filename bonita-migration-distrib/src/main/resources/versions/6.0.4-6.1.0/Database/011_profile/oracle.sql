--
-- PROFILE
-- 

ALTER TABLE "PROFILE" ADD "ISDEFAULT" NUMBER(1) DEFAULT 0 NOT NULL @@
ALTER TABLE "PROFILE" ADD "CREATIONDATE" NUMBER(19,0) DEFAULT 0 NOT NULL @@
ALTER TABLE "PROFILE" ADD "CREATEDBY" NUMBER(19,0) DEFAULT 0 NOT NULL @@
ALTER TABLE "PROFILE" ADD "LASTUPDATEDATE" NUMBER(19,0) DEFAULT 0 NOT NULL @@
ALTER TABLE "PROFILE" ADD "LASTUPDATEDBY" NUMBER(19,0) DEFAULT 0 NOT NULL @@


--
-- PROFILEENTRY
-- 

-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE "PROFILEENTRY" DISABLE UNIQUE (tenantId, parentId, profileId, name) CASCADE @@
ALTER TABLE "PROFILEENTRY" DROP UNIQUE (tenantId, parentId, profileId, name) @@

ALTER TABLE "PROFILEENTRY" MODIFY "NAME" NULL @@
CREATE INDEX "INDEXPROFILEENTRY" ON "PROFILEENTRY" ("TENANTID" ASC, "PARENTID" ASC, "PROFILEID" ASC) @@


--
-- Datas
--

UPDATE profile 
	SET creationDate = :creationDate,
	createdBy = -1, 
	lastUpdateDate = :creationDate,
	lastUpdatedBy = -1,
	isDefault = 1
	WHERE name in ('User', 'Administrator') @@
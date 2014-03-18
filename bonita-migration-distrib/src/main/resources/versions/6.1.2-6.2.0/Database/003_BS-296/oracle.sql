-- WARNING: If the index below is backing a unique/primary key constraint this DROP INDEX statement may cause an error if the constraint was already dropped
ALTER TABLE "data_mapping" DISABLE UNIQUE (containerid, containertype, dataname) CASCADE @@
ALTER TABLE "data_mapping" DROP UNIQUE (containerid, containertype, dataname) @@

ALTER TABLE "data_mapping" ADD CONSTRAINT UNIQUE (tenantid,containerid,containertype,dataname)@@
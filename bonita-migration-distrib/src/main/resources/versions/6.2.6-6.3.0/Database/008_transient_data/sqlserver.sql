ALTER TABLE datasource DROP CONSTRAINT fk_datasource_tenantId
@@
ALTER TABLE datasourceparameter DROP CONSTRAINT fk_datasourceparameter_tenantId
@@
DROP TABLE datasourceparameter
@@
DROP TABLE datasource
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 50
@@
DELETE FROM "SEQUENCE" WHERE "ID" = 51
@@
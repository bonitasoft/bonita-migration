ALTER TABLE datasource DROP FOREIGN KEY fk_datasource_tenantId@@
ALTER TABLE datasourceparameter DROP FOREIGN KEY fk_datasourceparameter_tenantId@@
DROP INDEX fk_datasource_tenantId_idx on datasource@@
DROP INDEX fk_datasourceparameter_tenantId_idx on datasourceparameter@@
DROP TABLE datasourceparameter@@
DROP TABLE datasource@@
DELETE FROM sequence WHERE id = 50@@
DELETE FROM sequence WHERE id = 51@@
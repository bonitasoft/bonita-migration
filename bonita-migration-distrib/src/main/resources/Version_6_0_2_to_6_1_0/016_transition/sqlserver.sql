--
-- FOREIGN KEYS [DROP]
-- 

ALTER TABLE transition_instance DROP CONSTRAINT fk_transition_instance_tenantId
@@


--
-- transition_instance
-- 

DROP TABLE transition_instance
@@

DELETE FROM "SEQUENCE" WHERE "ID" = 10013
@@
--
-- FOREIGN KEYS [DROP]
-- 

ALTER TABLE transition_instance DROP CONSTRAINT fk_transition_instance_tenantId
GO


--
-- transition_instance
-- 

DROP TABLE transition_instance
GO

DELETE FROM "SEQUENCE" WHERE "ID" = 10013
GO
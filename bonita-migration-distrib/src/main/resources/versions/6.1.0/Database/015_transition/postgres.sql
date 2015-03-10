--
-- FOREIGN KEYS [DROP]
-- 

ALTER TABLE transition_instance DROP CONSTRAINT fk_transition_instance_tenantid;


--
-- transition_instance
-- 

DROP TABLE transition_instance;
DELETE FROM sequence WHERE id = 10013;
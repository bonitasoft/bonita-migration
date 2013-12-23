ALTER TABLE transition_instance DROP FOREIGN KEY fk_transition_instance_tenantId;
DROP TABLE transition_instance;
DELETE FROM sequence WHERE id = 10013;
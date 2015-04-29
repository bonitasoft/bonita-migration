ALTER TABLE message_instance DROP FOREIGN KEY fk_message_instance_tenantId
@@
ANALYZE TABLE message_instance
@@
ANALYZE TABLE tenant
@@

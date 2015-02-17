ALTER TABLE process_instance ALTER COLUMN stringIndex1 NVARCHAR(255) @@
ALTER TABLE process_instance ALTER COLUMN stringIndex2 NVARCHAR(255) @@
ALTER TABLE process_instance ALTER COLUMN stringIndex3 NVARCHAR(255) @@
ALTER TABLE process_instance ALTER COLUMN stringIndex4 NVARCHAR(255) @@
ALTER TABLE process_instance ALTER COLUMN stringIndex5 NVARCHAR(255) @@

ALTER TABLE arch_process_instance ALTER COLUMN stringIndex1 NVARCHAR(255) @@
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex2 NVARCHAR(255) @@
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex3 NVARCHAR(255) @@
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex4 NVARCHAR(255) @@
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex5 NVARCHAR(255) @@

ALTER TABLE group_ ALTER COLUMN name NVARCHAR(125) @@
ALTER TABLE group_ ALTER COLUMN displayName NVARCHAR(255) @@

ALTER TABLE role ALTER COLUMN name NVARCHAR(255) @@
ALTER TABLE role ALTER COLUMN displayName NVARCHAR(255) @@

ALTER TABLE user_ DROP COLUMN delegeeUserName @@

ALTER TABLE user_contactinfo ALTER COLUMN city NVARCHAR(255) @@
ALTER TABLE user_contactinfo ALTER COLUMN state NVARCHAR(255) @@
ALTER TABLE user_contactinfo ALTER COLUMN country NVARCHAR(255) @@
ALTER TABLE user_contactinfo ALTER COLUMN website NVARCHAR(255) @@

ALTER TABLE flownode_instance ALTER COLUMN name NVARCHAR(255) @@
ALTER TABLE flownode_instance ALTER COLUMN displayName NVARCHAR(255) @@
ALTER TABLE arch_flownode_instance ALTER COLUMN name NVARCHAR(255) @@
ALTER TABLE arch_flownode_instance ALTER COLUMN displayName NVARCHAR(255) @@

ALTER TABLE process_instance ALTER COLUMN stringIndex1 TYPE VARCHAR(255);
ALTER TABLE process_instance ALTER COLUMN stringIndex2 TYPE VARCHAR(255);
ALTER TABLE process_instance ALTER COLUMN stringIndex3 TYPE VARCHAR(255);
ALTER TABLE process_instance ALTER COLUMN stringIndex4 TYPE VARCHAR(255);
ALTER TABLE process_instance ALTER COLUMN stringIndex5 TYPE VARCHAR(255);

ALTER TABLE arch_process_instance ALTER COLUMN stringIndex1 TYPE VARCHAR(255);
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex2 TYPE VARCHAR(255);
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex3 TYPE VARCHAR(255);
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex4 TYPE VARCHAR(255);
ALTER TABLE arch_process_instance ALTER COLUMN stringIndex5 TYPE VARCHAR(255);

ALTER TABLE group_ ALTER COLUMN name TYPE VARCHAR(125);
ALTER TABLE group_ ALTER COLUMN displayName TYPE VARCHAR(255);

ALTER TABLE role ALTER COLUMN name TYPE VARCHAR(255);
ALTER TABLE role ALTER COLUMN displayName TYPE VARCHAR(255);

ALTER TABLE user_ DROP COLUMN delegeeUserName;

ALTER TABLE user_contactinfo ALTER COLUMN city TYPE VARCHAR(255);
ALTER TABLE user_contactinfo ALTER COLUMN state TYPE VARCHAR(255);
ALTER TABLE user_contactinfo ALTER COLUMN country TYPE VARCHAR(255);
ALTER TABLE user_contactinfo ALTER COLUMN website TYPE VARCHAR(255);

ALTER TABLE flownode_instance ALTER COLUMN name TYPE VARCHAR(255);
ALTER TABLE flownode_instance ALTER COLUMN displayName TYPE VARCHAR(255);
ALTER TABLE arch_flownode_instance ALTER COLUMN name TYPE VARCHAR(255);
ALTER TABLE arch_flownode_instance ALTER COLUMN displayName TYPE VARCHAR(255);
ALTER TABLE arch_transition_instance DROP COLUMN name;

ALTER TABLE process_comment ALTER COLUMN content TYPE VARCHAR(512);
ALTER TABLE arch_process_comment ALTER COLUMN content TYPE VARCHAR(512);

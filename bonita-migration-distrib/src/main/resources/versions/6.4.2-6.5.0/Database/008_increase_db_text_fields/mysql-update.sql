ALTER TABLE process_instance MODIFY COLUMN stringIndex1 VARCHAR(255);
ALTER TABLE process_instance MODIFY COLUMN stringIndex2 VARCHAR(255);
ALTER TABLE process_instance MODIFY COLUMN stringIndex3 VARCHAR(255);
ALTER TABLE process_instance MODIFY COLUMN stringIndex4 VARCHAR(255);
ALTER TABLE process_instance MODIFY COLUMN stringIndex5 VARCHAR(255);

ALTER TABLE arch_process_instance MODIFY COLUMN stringIndex1 VARCHAR(255);
ALTER TABLE arch_process_instance MODIFY COLUMN stringIndex2 VARCHAR(255);
ALTER TABLE arch_process_instance MODIFY COLUMN stringIndex3 VARCHAR(255);
ALTER TABLE arch_process_instance MODIFY COLUMN stringIndex4 VARCHAR(255);
ALTER TABLE arch_process_instance MODIFY COLUMN stringIndex5 VARCHAR(255);

ALTER TABLE group_ MODIFY COLUMN name VARCHAR(125);
ALTER TABLE group_ MODIFY COLUMN displayName VARCHAR(255);

ALTER TABLE role MODIFY COLUMN name VARCHAR(255);
ALTER TABLE role MODIFY COLUMN displayName VARCHAR(255);

ALTER TABLE user_ MODIFY COLUMN delegeeUserName VARCHAR(255);

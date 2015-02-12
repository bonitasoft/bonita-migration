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

ALTER TABLE group_ MODIFY COLUMN name VARCHAR(255);
ALTER TABLE group_ MODIFY COLUMN displayName VARCHAR(255);
ALTER TABLE group_ MODIFY COLUMN parentPath VARCHAR(1024);
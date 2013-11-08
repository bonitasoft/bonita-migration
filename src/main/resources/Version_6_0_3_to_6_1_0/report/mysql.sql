ALTER TABLE report ADD lastModificationDate BIGINT(20) NOT NULL;
ALTER TABLE report ADD screenshot MEDIUMBLOB;
ALTER TABLE report ADD content LONGBLOB;
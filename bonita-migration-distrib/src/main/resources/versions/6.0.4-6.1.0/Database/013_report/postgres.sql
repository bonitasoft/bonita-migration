ALTER TABLE report ADD lastmodificationdate INT8 NOT NULL DEFAULT 0;
ALTER TABLE report ADD screenshot BYTEA;
ALTER TABLE report ADD content BYTEA;
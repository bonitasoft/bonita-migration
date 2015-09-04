CREATE TABLE ref_biz_data_inst2 (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id BIGINT,
  	fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(255) NOT NULL
) ENGINE = INNODB
@@

INSERT INTO ref_biz_data_inst2(tenantid, id, name, data_id, proc_inst_id, fn_inst_id, data_classname, kind)
SELECT r.tenantid, r.id, r.name, r.data_id, p.id, NULL, r.data_classname, 'proc_simple_ref'
FROM ref_biz_data_inst r left join process_instance p on  r.proc_inst_id=p.id and r.tenantid=p.tenantid
WHERE p.ID is not null
@@

DROP TABLE ref_biz_data_inst
@@

RENAME TABLE  ref_biz_data_inst2 TO ref_biz_data_inst
@@

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id)
@@

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id)
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id)
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uk_ref_biz_data_inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantid)
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE
@@

CREATE TABLE multi_biz_data (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	idx BIGINT NOT NULL,
  	data_id BIGINT NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
) ENGINE = INNODB;
@@

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;
@@
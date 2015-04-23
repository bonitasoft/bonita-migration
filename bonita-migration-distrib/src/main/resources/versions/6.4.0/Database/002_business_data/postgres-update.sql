CREATE TABLE ref_biz_data_inst2 (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id INT8,
  	fn_inst_id INT8,
  	data_id INT8,
  	data_classname VARCHAR(255) NOT NULL
);
@@

INSERT INTO ref_biz_data_inst2(tenantid, id, name, data_id, fn_inst_id, data_classname, kind) 
SELECT tenantid, id, name, data_id, NULL, data_classname, 'simple_ref' 
FROM ref_biz_data_inst
@@

DROP TABLE ref_biz_data_inst
@@

ALTER TABLE ref_biz_data_inst2 RENAME TO ref_biz_data_inst
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
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	idx INT8 NOT NULL,
  	data_id INT8 NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
);
@@

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE;
@@
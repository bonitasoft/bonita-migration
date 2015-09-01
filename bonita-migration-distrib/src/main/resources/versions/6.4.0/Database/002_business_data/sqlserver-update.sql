CREATE TABLE ref_biz_data_inst2 (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	kind NVARCHAR(15) NOT NULL,
  	name NVARCHAR(255) NOT NULL,
  	proc_inst_id NUMERIC(19, 0),
  	fn_inst_id NUMERIC(19, 0),
  	data_id  NUMERIC(19, 0),
  	data_classname NVARCHAR(255) NOT NULL
)
@@

INSERT INTO ref_biz_data_inst2(tenantid, id, name, data_id, proc_inst_id, fn_inst_id, data_classname, kind)
SELECT tenantid, id, name, data_id, proc_inst_id, NULL, data_classname, 'simple_ref'
FROM ref_biz_data_inst
@@

DROP TABLE ref_biz_data_inst
@@

sp_rename ref_biz_data_inst2 , ref_biz_data_inst
@@

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id)
@@

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id)
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data PRIMARY KEY (tenantid, id)
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT  uk_ref_biz_data UNIQUE (name, proc_inst_id, fn_inst_id, tenantid )
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE
@@

CREATE TABLE multi_biz_data (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	idx NUMERIC(19, 0) NOT NULL,
  	data_id NUMERIC(19, 0) NOT NULL,
  	PRIMARY KEY (tenantid, id, data_id)
)
@@

ALTER TABLE multi_biz_data ADD CONSTRAINT fk_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES ref_biz_data_inst(tenantid, id) ON DELETE CASCADE
@@
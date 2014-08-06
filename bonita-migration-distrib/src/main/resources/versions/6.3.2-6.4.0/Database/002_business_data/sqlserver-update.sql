ALTER TABLE ref_biz_data_inst ADD kind NVARCHAR(15)
@@

ALTER TABLE ref_biz_data_inst ADD fn_inst_id NUMERIC(19, 0)
@@

UPDATE ref_biz_data_inst SET kind='simple_ref'
@@

ALTER TABLE ref_biz_data_inst ALTER COLUMN kind NVARCHAR(10) NOT NULL
@@

ALTER TABLE ref_biz_data_inst ALTER COLUMN proc_inst_id NUMERIC(19, 0) NULL
@@

CREATE UNIQUE INDEX uniq_ref_biz_data_proc ON ref_biz_data_inst(tenantid, proc_inst_id, name)
@@

CREATE UNIQUE INDEX uniq_ref_biz_data_fn ON ref_biz_data_inst(tenantid, fn_inst_id, name)
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
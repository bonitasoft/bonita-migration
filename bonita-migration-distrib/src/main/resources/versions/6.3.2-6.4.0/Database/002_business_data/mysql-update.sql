ALTER TABLE ref_biz_data_inst ADD COLUMN kind VARCHAR(15);
@@

ALTER TABLE ref_biz_data_inst ADD COLUMN fn_inst_id BIGINT;
@@

UPDATE ref_biz_data_inst SET kind="simple_ref";
@@

ALTER TABLE ref_biz_data_inst MODIFY COLUMN kind VARCHAR(15) NOT NULL;
@@

ALTER TABLE ref_biz_data_inst MODIFY COLUMN proc_inst_id BIGINT NULL;
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uniq_ref_biz_data_proc UNIQUE (tenantid, proc_inst_id, name);
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uniq_ref_biz_data_fn UNIQUE (tenantid, fn_inst_id, name);
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_proc FOREIGN KEY (tenantid, proc_inst_id) REFERENCES process_instance(tenantid, id) ON DELETE CASCADE;
@@

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT fk_ref_biz_data_fn FOREIGN KEY (tenantid, fn_inst_id) REFERENCES flownode_instance(tenantid, id) ON DELETE CASCADE;
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
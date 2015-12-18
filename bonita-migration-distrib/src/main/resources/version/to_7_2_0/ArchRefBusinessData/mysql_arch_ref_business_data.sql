CREATE TABLE arch_ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	orig_proc_inst_id BIGINT,
  	orig_fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(255) NOT NULL
)
@@
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id)
@@
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id)
@@
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id)
@@
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT uk_arch_ref_biz_data_inst UNIQUE (name, orig_proc_inst_id, orig_fn_inst_id, tenantid)
@@
CREATE TABLE arch_multi_biz_data (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	idx BIGINT NOT NULL,
  	data_id BIGINT NOT NULL
)
@@
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id)
@@
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE
@@
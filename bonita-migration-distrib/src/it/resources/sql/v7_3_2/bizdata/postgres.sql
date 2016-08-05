CREATE TABLE ref_biz_data_inst (
	tenantid INT8 NOT NULL,
  	id INT8 NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	proc_inst_id INT8,
  	fn_inst_id INT8,
  	data_id INT8,
  	data_classname VARCHAR(255) NOT NULL
);

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);

CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);

ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uk_ref_biz_data_inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantid);

CREATE TABLE arch_ref_biz_data_inst (
	tenantid BIGINT NOT NULL,
  	id BIGINT NOT NULL,
  	kind VARCHAR(15) NOT NULL,
  	name VARCHAR(255) NOT NULL,
  	orig_proc_inst_id BIGINT,
  	orig_fn_inst_id BIGINT,
  	data_id BIGINT,
  	data_classname VARCHAR(255) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT uk_arch_ref_biz_data_inst UNIQUE (name, orig_proc_inst_id, orig_fn_inst_id, tenantid);

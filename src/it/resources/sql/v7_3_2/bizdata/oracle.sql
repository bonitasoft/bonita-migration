CREATE TABLE ref_biz_data_inst (
	tenantid NUMBER(19, 0) NOT NULL,
  	id NUMBER(19, 0) NOT NULL,
  	kind VARCHAR2(15 CHAR) NOT NULL,
  	name VARCHAR2(255 CHAR) NOT NULL,
  	proc_inst_id NUMBER(19, 0),
  	fn_inst_id NUMBER(19, 0),
  	data_id NUMBER(19, 0),
  	data_classname VARCHAR2(255 CHAR) NOT NULL
);

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id);
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT UK_Ref_Biz_Data_Inst UNIQUE (name, proc_inst_id, fn_inst_id, tenantId);

CREATE TABLE arch_ref_biz_data_inst (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    kind VARCHAR2(15 CHAR) NOT NULL,
    name VARCHAR2(255 CHAR) NOT NULL,
    orig_proc_inst_id NUMBER(19, 0),
    orig_fn_inst_id NUMBER(19, 0),
    data_id NUMBER(19, 0),
    data_classname VARCHAR2(255 CHAR) NOT NULL
);
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id);
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id);
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT uk_arch_ref_biz_data_inst UNIQUE (name, orig_proc_inst_id, orig_fn_inst_id, tenantid);

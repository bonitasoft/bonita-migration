CREATE TABLE ref_biz_data_inst (
	tenantid NUMERIC(19, 0) NOT NULL,
  	id NUMERIC(19, 0) NOT NULL,
  	kind NVARCHAR(15) NOT NULL,
  	name NVARCHAR(255) NOT NULL,
  	proc_inst_id NUMERIC(19, 0),
  	fn_inst_id NUMERIC(19, 0),
  	data_id NUMERIC(19, 0),
  	data_classname NVARCHAR(255) NOT NULL
)
GO

CREATE INDEX idx_biz_data_inst1 ON ref_biz_data_inst (tenantid, proc_inst_id)
GO
CREATE INDEX idx_biz_data_inst2 ON ref_biz_data_inst (tenantid, fn_inst_id)
GO
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT pk_ref_biz_data PRIMARY KEY (tenantid, id)
GO
ALTER TABLE ref_biz_data_inst ADD CONSTRAINT uk_ref_biz_data UNIQUE (name, proc_inst_id, fn_inst_id, tenantid)
GO

CREATE TABLE arch_ref_biz_data_inst (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    kind NVARCHAR(15) NOT NULL,
    name NVARCHAR(255) NOT NULL,
    orig_proc_inst_id NUMERIC(19, 0),
    orig_fn_inst_id NUMERIC(19, 0),
    data_id NUMERIC(19, 0),
    data_classname NVARCHAR(255) NOT NULL
)
GO
CREATE INDEX idx_arch_biz_data_inst1 ON arch_ref_biz_data_inst (tenantid, orig_proc_inst_id)
GO
CREATE INDEX idx_arch_biz_data_inst2 ON arch_ref_biz_data_inst (tenantid, orig_fn_inst_id)
GO
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT pk_arch_ref_biz_data_inst PRIMARY KEY (tenantid, id)
GO
ALTER TABLE arch_ref_biz_data_inst ADD CONSTRAINT uk_arch_ref_biz_data_inst UNIQUE (name, orig_proc_inst_id, orig_fn_inst_id, tenantid)
GO
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
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    idx NUMERIC(19, 0) NOT NULL,
    data_id NUMERIC(19, 0) NOT NULL
)
@@
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT pk_arch_rbdi_mbd PRIMARY KEY (tenantid, id, data_id)
@@
ALTER TABLE arch_multi_biz_data ADD CONSTRAINT fk_arch_rbdi_mbd FOREIGN KEY (tenantid, id) REFERENCES arch_ref_biz_data_inst(tenantid, id) ON DELETE CASCADE
@@
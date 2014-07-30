ALTER TABLE ref_biz_data_inst ADD kind NVARCHAR(10)
@@

UPDATE ref_biz_data_inst SET kind='simple_ref'
@@

ALTER TABLE ref_biz_data_inst ALTER COLUMN kind NVARCHAR(10) NOT NULL
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

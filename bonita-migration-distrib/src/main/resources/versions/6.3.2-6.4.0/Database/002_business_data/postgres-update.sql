ALTER TABLE ref_biz_data_inst ADD COLUMN kind VARCHAR(10);
@@
UPDATE ref_biz_data_inst SET kind='simple_ref';
@@
ALTER TABLE ref_biz_data_inst ALTER COLUMN kind SET NOT NULL;
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
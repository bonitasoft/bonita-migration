CREATE TABLE ref_biz_data_inst (
    tenantid NUMERIC(19, 0) NOT NULL,
    id NUMERIC(19, 0) NOT NULL,
    name NVARCHAR(255) NOT NULL,
    proc_inst_id NUMERIC(19, 0) NOT NULL,
    data_id INT NULL,
    data_classname NVARCHAR(255) NOT NULL,
    UNIQUE (tenantid, proc_inst_id, name),
    PRIMARY KEY (tenantid, id)
) @@

INSERT INTO sequence VALUES(${tenantid}, 10096, 1) @@
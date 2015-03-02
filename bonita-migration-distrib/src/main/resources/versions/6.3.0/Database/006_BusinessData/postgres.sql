CREATE TABLE ref_biz_data_inst (
    tenantid INT8 NOT NULL,
    id INT8 NOT NULL,
    name VARCHAR(255) NOT NULL,
    proc_inst_id INT8 NOT NULL,
    data_id INT NULL,
    data_classname VARCHAR(255) NOT NULL,
    UNIQUE (tenantid, proc_inst_id, name),
    PRIMARY KEY (tenantid, id)
);
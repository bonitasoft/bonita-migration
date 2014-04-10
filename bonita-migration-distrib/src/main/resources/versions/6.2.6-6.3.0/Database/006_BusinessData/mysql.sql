CREATE TABLE ref_biz_data_inst (
    tenantid BIGINT NOT NULL,
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    proc_inst_id BIGINT NOT NULL,
    data_id INT NULL,
    data_classname VARCHAR(255) NOT NULL,
    UNIQUE (tenantid, proc_inst_id, name),
    PRIMARY KEY (tenantid, id)
) ENGINE = INNODB;
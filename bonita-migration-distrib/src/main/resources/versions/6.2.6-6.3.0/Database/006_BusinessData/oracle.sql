CREATE TABLE ref_biz_data_inst (
    tenantid NUMBER(19, 0) NOT NULL,
    id NUMBER(19, 0) NOT NULL,
    name VARCHAR2(255) NOT NULL,
    proc_inst_id NUMBER(19, 0) NOT NULL,
    data_id INT NULL,
    data_classname VARCHAR2(255) NOT NULL,
    UNIQUE (tenantid, proc_inst_id, name),
    PRIMARY KEY (tenantid, id)
) @@

INSERT INTO sequence VALUES(${tenantid}, 10096, 1) @@
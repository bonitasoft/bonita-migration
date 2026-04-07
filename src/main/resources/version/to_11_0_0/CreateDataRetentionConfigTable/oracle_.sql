CREATE TABLE data_retention_config (
    id                  NUMBER(19, 0) NOT NULL,
    data_classname      VARCHAR2(255 CHAR) NOT NULL,
    reference_date      VARCHAR2(20 CHAR) NOT NULL,
    retention_days      INT NOT NULL,
    created_at          NUMBER(19, 0) NOT NULL,
    updated_at          NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_data_retention_config PRIMARY KEY (id),
    CONSTRAINT uk_data_retention_config_classname UNIQUE (data_classname)
)

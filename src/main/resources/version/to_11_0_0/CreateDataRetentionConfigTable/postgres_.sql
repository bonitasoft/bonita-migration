CREATE TABLE data_retention_config (
    id                  INT8 NOT NULL,
    data_classname      VARCHAR(255) NOT NULL,
    reference_date      VARCHAR(20) NOT NULL,
    retention_days      INT NOT NULL,
    created_at          INT8 NOT NULL,
    updated_at          INT8 NOT NULL,
    CONSTRAINT pk_data_retention_config PRIMARY KEY (id),
    CONSTRAINT uk_data_retention_config_classname UNIQUE (data_classname)
)

create index idx_qrtz_t_nf_time on QRTZ_TRIGGERS(NEXT_FIRE_TIME)
@@
create index idx_qrtz_t_state on QRTZ_TRIGGERS(TRIGGER_STATE)
@@
create index idx_qrtz_t_nf_st on QRTZ_TRIGGERS(TRIGGER_STATE,NEXT_FIRE_TIME)
@@
create index idx_qrtz_ft_trig_name on QRTZ_FIRED_TRIGGERS(TRIGGER_NAME)
@@
create index idx_qrtz_ft_trig_group on QRTZ_FIRED_TRIGGERS(TRIGGER_GROUP)
@@
create index idx_qrtz_ft_trig_n_g on QRTZ_FIRED_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
@@
create index idx_qrtz_ft_trig_inst_name on QRTZ_FIRED_TRIGGERS(INSTANCE_NAME)
@@
create index idx_qrtz_ft_job_name on QRTZ_FIRED_TRIGGERS(JOB_NAME)
@@
create index idx_qrtz_ft_job_group on QRTZ_FIRED_TRIGGERS(JOB_GROUP)
@@
create index idx_qrtz_t_nf_time_misfire on QRTZ_TRIGGERS(MISFIRE_INSTR,NEXT_FIRE_TIME)
@@
create index idx_qrtz_t_nf_st_misfire on QRTZ_TRIGGERS(MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE)
@@
create index idx_qrtz_t_nf_st_misfire_grp on QRTZ_TRIGGERS(MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE)
@@
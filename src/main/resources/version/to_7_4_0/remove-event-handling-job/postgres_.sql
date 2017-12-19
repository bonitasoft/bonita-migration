delete from QRTZ_CRON_TRIGGERS where TRIGGER_NAME in (select t.TRIGGER_NAME from QRTZ_TRIGGERS t where t.JOB_NAME = 'BPMEventHandling');
delete from QRTZ_FIRED_TRIGGERS where TRIGGER_NAME in (select t.TRIGGER_NAME from QRTZ_TRIGGERS t where t.JOB_NAME = 'BPMEventHandling');
delete from QRTZ_TRIGGERS where JOB_NAME = 'BPMEventHandling';
delete from QRTZ_JOB_DETAILS where JOB_NAME = 'BPMEventHandling';
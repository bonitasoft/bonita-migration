import java.io.ObjectInputStream;
import java.util.Map;

import org.quartz.JobDataMap;

import groovy.sql.Sql;

import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.core.IOUtil;

def map = [:]
println "Change how quartz store job details"
sql.eachRow("select * from QRTZ_JOB_DETAILS",{row ->
    def content;
    if(dbVendor == "oracle"){
        def blob = row.JOB_DATA
        content= blob.getBytes(1l,blob.length().intValue())
    }else{
        content = row.JOB_DATA
    }
    map.putAt([row.JOB_NAME, row.JOB_GROUP], content)
});

def theClassLoader = JobDataMap.class.getClassLoader()
map.each {
    //give the same class loader as the one of the class we deserialize
    def JobDataMap jobData = IOUtil.deserialize(it.value, theClassLoader)
    def long jobId = jobData.get("jobId")
    def long tenantId = jobData.get("tenantId")
    def String jobName = jobData.get("jobName")
    def ByteArrayOutputStream ba = new ByteArrayOutputStream()
    def Properties props = new Properties()
    jobData.each { entry ->
        props.put(entry.key,String.valueOf(entry.value))
    }
    props.store(ba, "")
    def out = ba.toByteArray()
    println "Update job <$jobName> of tenant <$tenantId> in quartz table"

    def rowUpdated = sql.executeUpdate(MigrationUtil.getSqlContent(MigrationUtil.getSqlFile(feature, dbVendor, "updateJobData").text, [":JOB_NAME":it.key[0],":JOB_GROUP":it.key[1]])[0], [out] as Object[])
    if(rowUpdated != 1){
        throw new IllegalStateException("Unable to update job <$jobName> in quartz table expected <1> row update had <$rowUpdated>")
    }
}

println "Change the misfire instructions of jobs"

def backgroundJobs = [
    "BPMEventHandling",
    "CleanInvalidSessions",
    "DeleteBatchJob",
    "InsertBatchLogsJob"
];
def int RESTART_ALL = -1
def int RESTART_NONE = 2
sql.eachRow("select * from QRTZ_TRIGGERS",{row ->
    //if the job is a bonita internal background job we say that we don't restart missed executions
    def int flag = row.JOB_NAME in backgroundJobs ? RESTART_NONE : RESTART_ALL;
    def rowUpdated = sql.executeUpdate(
        MigrationUtil.getSqlContent("UPDATE QRTZ_TRIGGERS SET MISFIRE_INSTR = ? WHERE JOB_NAME = ':JOB_NAME' AND JOB_GROUP = ':JOB_GROUP'",
             [":JOB_NAME":row.JOB_NAME,":JOB_GROUP":row.JOB_GROUP])[0],
         flag)
    println "Update the trigger <${row.JOB_NAME}> with flag $flag (=${flag==2?'RESTART_NONE':'RESTART_ALL'})"
    if(rowUpdated != 1){
        throw new IllegalStateException("Unable to update trigger <${row.JOB_NAME}> in quartz table expected <1> row update had <$rowUpdated>")
    }
})


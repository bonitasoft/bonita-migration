import java.io.ObjectInputStream;
import java.util.Map;

import org.quartz.JobDataMap;

import groovy.sql.Sql;

import org.bonitasoft.engine.scheduler.JobIdentifier;
import org.bonitasoft.migration.core.MigrationUtil;
import org.bonitasoft.migration.core.IOUtil;


MigrationUtil.executeDefaultSqlFile(feature, dbVendor, sql);

def nonConcurrentJobs = [
    "BPMEventHandling",
    "CleanInvalidSessions"
];
def nonConcurrentJobClassName = "org.bonitasoft.engine.scheduler.impl.NonConcurrentQuartzJob"
def concurrentJobClassName = "org.bonitasoft.engine.scheduler.impl.ConcurrentQuartzJob"
def map = [:]

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

def jobIdentifierClassLoader = JobIdentifier.class.getClassLoader()
map.each {
    //give the same class loader as the one of the class we deserialize
    def JobDataMap jobData = IOUtil.deserialize(it.value, jobIdentifierClassLoader);
    def uncasted = jobData.get("jobIdentifier");
    def JobIdentifier jobIdent = uncasted;
    def newMap = new JobDataMap();
    newMap.put("jobId", jobIdent.id);
    newMap.put("tenantId", jobIdent.tenantId);
    newMap.put("jobName", jobIdent.jobName);
    byte[] out = IOUtil.serialize(newMap);
    println "Update job <$jobIdent.jobName> of tenant <$jobIdent.tenantId> in quartz table"
    //change the job class name to be concurrent or not
    def jobClassName = jobIdent.jobName in nonConcurrentJobs ? nonConcurrentJobClassName: concurrentJobClassName
    def rowUpdated = sql.executeUpdate(MigrationUtil.getSqlContent(MigrationUtil.getSqlFile(feature, dbVendor, "updateJobData").text, [":JOB_NAME":it.key[0],":JOB_GROUP":it.key[1],":JOB_CLASS_NAME":jobClassName])[0], [out] as Object[])
    if(rowUpdated != 1){
        throw new IllegalStateException("Unable to update job <$jobIdent.jobName> in quartz table expected <1> row update had <$rowUpdated>")
    }
}


import org.bonitasoft.migration.core.MigrationUtil;

def currentTime = System.currentTimeMillis()

// Portal
def zip = new File(feature, "bonita-portal-theme.zip")
def css = new File(feature, "bonita.css")

def parameters = new HashMap()
parameters.put(":content", zip.bytes)
parameters.put(":cssContent", css.bytes)
parameters.put(":type", "PORTAL")
parameters.put(":lastUpdateDate", currentTime)
MigrationUtil.executeSqlFile(feature, dbVendor, "update", parameters, sql, false)


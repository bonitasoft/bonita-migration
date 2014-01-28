import org.bonitasoft.migration.core.MigrationUtil;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.ByteArrayOutputStream;

def currentTime = System.currentTimeMillis()

sql.eachRow("SELECT * from theme WHERE isDefault = false AND type = 'PORTAL'") { row ->
    def byte[] content = row.getAt("content")
    def tenantId = row.getAt("tenantId")
    def rowId = row.getAt("id")
    println "Update content of theme ${rowId}"
    byte[] buffer = new byte[1024];
    def newZipContent;
    def ZipInputStream zipIS = new ZipInputStream(new ByteArrayInputStream(content));
    zipIS.withStream { s ->
        modifiedZipBOS = new ByteArrayOutputStream();
        new ZipOutputStream(modifiedZipBOS).withStream { zous ->
            for(ZipEntry ze = s.getNextEntry(); ze != null; ze = s.getNextEntry()) {
                def ByteArrayOutputStream bos = new ByteArrayOutputStream()
                bos.withStream { out ->
                    for(int read = s.read(buffer); read > -1; read = s.read(buffer)) {
                        // FIXME: use 0 instead of -1 ?
                        out.write(buffer, 0, read);
                    }
                    def byte[] fileContent  = bos.toByteArray()
                    if(ze.getName().equals("BonitaConsole.html")){
                        println "Replace jquery+ to jqueryplus in BonitaConsole.html of theme ${rowId}"
                        fileContent = new String(fileContent).replace("jquery+/", "jqueryplus/").getBytes()
                    }
                    //write bytes to zous
                    def newZe = new ZipEntry(ze.getName())
                    //reset the size
                    zous.putNextEntry(newZe)
                    zous.write(fileContent, 0, fileContent.length)
                    zous.closeEntry()
                }
            }
            newZipContent = modifiedZipBOS.toByteArray()
        }
    }
    sql.executeUpdate("UPDATE theme SET content = ? WHERE tenantid = ? AND id = ?", newZipContent, tenantId, rowId)
}

println "Migration of custom themes done"

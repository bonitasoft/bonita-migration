import org.bonitasoft.migration.core.IOUtil

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

def currentTime = System.currentTimeMillis()

sql.eachRow("SELECT * from theme WHERE isDefault = ${false} AND type = ${"PORTAL"}") { row ->
    def byte[] content
    if (dbVendor == "oracle") {
        def blob = row.getAt("content")
        content = blob.getBytes(1l, blob.length().intValue())
    } else {
        content = row.getAt("content")
    }
    def tenantId = row.getAt("tenantId")
    def rowId = row.getAt("id")
    println "Update content of theme ${rowId}"
    byte[] buffer = new byte[1024];
    def newZipContent;
    def shouldUpdate = false
    def ZipInputStream zipIS = new ZipInputStream(new ByteArrayInputStream(content));
    zipIS.withStream { s ->
        modifiedZipBOS = new ByteArrayOutputStream();
        new ZipOutputStream(modifiedZipBOS).withStream { zous ->

            def containsScriptFolder = false
            for (ZipEntry ze = s.getNextEntry(); ze != null; ze = s.getNextEntry()) {
                def ByteArrayOutputStream bos = new ByteArrayOutputStream()
                bos.withStream { out ->
                    for (int read = s.read(buffer); read > -1; read = s.read(buffer)) {
                        out.write(buffer, 0, read);
                    }
                    def byte[] fileContent = bos.toByteArray()
                    if(ze.getName().startsWith("skin")){
                        containsScriptFolder = true
                    }
                    //write bytes to zous
                    def newZe = new ZipEntry(ze.getName())
                    //reset the size
                    zous.putNextEntry(newZe)
                    zous.write(fileContent, 0, fileContent.length)
                    zous.closeEntry()
                }
            }
            if(containsScriptFolder){
                shouldUpdate = true
                //get the content from the default theme
                def theme = new File(new File(feature.getParentFile(), "001_theme"), "bonita-portal-theme.zip")
                def defaultThemeContent = theme.bytes

                //add the new entries
                def stream = new ZipInputStream(new ByteArrayInputStream(defaultThemeContent))
                stream.withStream { defaultTheme ->
                    for (ZipEntry ze = defaultTheme.getNextEntry(); ze != null; ze = defaultTheme.getNextEntry()) {
                        def entryName = ze.getName()
                        if(entryName.startsWith("skin/bootstrap")
                                || entryName.startsWith("skin/fonts/glyphicons")
                                || entryName.equals("css/bootstrap.min.css")
                                || entryName.equals("scripts/bootstrap.min.js")){
                            def newZe = new ZipEntry(entryName)
                            //reset the size
                            zous.putNextEntry(newZe)
                            for (int read = defaultTheme.read(buffer); read > -1; read = defaultTheme.read(buffer)) {
                                zous.write(buffer, 0, read);
                            }
                            zous.closeEntry()
                        }
                    }
                }
            }
        }
        newZipContent = modifiedZipBOS.toByteArray()
    }
    if(shouldUpdate){
        //TODO backup

        def backup = new File(bonitaHome, "backup")
        backup.mkdir()
        def file = new File(backup, "theme-${rowId}.zip")
        FileOutputStream fileOuputStream = new FileOutputStream(file);
        fileOuputStream.write(content);
        fileOuputStream.close();
        println "The custom theme $rowId was updated, a backup was put in ${backup.getPath()}/theme-${rowId}.zip"
        sql.executeUpdate("UPDATE theme SET content = ? , lastUpdateDate = ? WHERE tenantid = ? AND id = ?", newZipContent, currentTime, tenantId, rowId)
    }else{
        println "The custom theme $rowId can't be updated, update it manually to add skin/bootstrap skin/font/ css/bootstrap.min.js scripts/bootstrap.min.js"
    }
}

println "Migration of custom themes done"

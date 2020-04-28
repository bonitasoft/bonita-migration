/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.migration.core

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 *
 * Util classes that contains common methods for I/O
 *
 * @author Celine Souchet
 *
 */
class IOUtil {

    private final static int DEFAULT_BUFFER_SIZE = 1024 * 4

    public static read = System.in.newReader().&readLine

    public final static String AUTO_ACCEPT = "auto.accept"

    private final static Logger log = new Logger()

    static boolean isAutoAccept() {
        return System.getProperty(AUTO_ACCEPT) == "true"
    }

    static void deleteDirectory(File dir) {
        if (dir == null) {
            throw new IllegalArgumentException("Can't execute migrateDirectory method with arguments : dir = " + dir)
        }

        log.info "Deleting all content of $dir..."

        if (!dir.deleteDir()) {
            throw new IllegalStateException("Migration failed. Unable to delete : " + dir)
        }
    }

    static void copyDirectory(File srcDir, File destDir) throws IOException {
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created")
            }
            destDir.setLastModified(srcDir.lastModified())
            if (!destDir.canWrite()) {
                throw new IOException("Destination '" + destDir + "' cannot be written to")
            }
        }
        // recurse
        File[] files = srcDir.listFiles()
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir)
        }
        for (int i = 0; i < files.length; i++) {
            File copiedFile = new File(destDir, files[i].getName())
            if (files[i].isDirectory()) {
                copyDirectory(files[i], copiedFile)
            } else {
                copyFile(files[i], copiedFile)
            }
        }
    }

    static void copyFile(File srcFile, File destFile) throws IOException {
        if (destFile.exists() && !(destFile.delete())) {
            throw new IllegalStateException("Migration failed. Unable to delete : " + destFile)
        }
        FileInputStream input = new FileInputStream(srcFile)
        try {
            FileOutputStream output = new FileOutputStream(destFile)
            try {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE]
                long count = 0
                int n = 0
                while (-1 != (n = input.read(buffer))) {
                    output.write(buffer, 0, n)
                    count += n
                }
            } finally {
                try {
                    if (output != null) {
                        output.close()
                    }
                } catch (IOException ignored) {
                    // ignore
                }
            }
        } finally {
            try {
                if (input != null) {
                    input.close()
                }
            } catch (IOException ignored) {
                // ignore
            }
        }
        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "'")
        }
        destFile.setLastModified(srcFile.lastModified())
    }

    static Object deserialize(byte[] bytes, ClassLoader theClassLoader) {
        //had to override the method of objectinputstream to be able to give the object classloader in input
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                return Class.forName(objectStreamClass.getName(), true, theClassLoader)
            }
        }
        try {
            return input.readObject()
        } finally {
            input.close()
        }
    }

    static byte[] serialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ObjectOutputStream out
        try {
            out = new ObjectOutputStream(baos)
            out.writeObject(object)
            out.flush()
        } finally {
            out.close()
        }
        return baos.toByteArray()
    }

    static void askIfWeContinue() {
        if (!isAutoAccept()) {
            log.info "Continue migration? (yes/no): "
            String input = read()
            if (input != "yes") {
                log.warn "Migration cancelled"
                System.exit(0)
            }
        }
    }

    static void unzip(InputStream inputStream, File outputDirectory) {

        byte[] buff = new byte[1024]
        inputStream.withStream { stream ->

            def zipStream = new ZipInputStream(stream)
            def ZipEntry entry
            while ((entry = zipStream.getNextEntry()) != null) {
                def file = new File(outputDirectory, entry.getName())
                if (entry.isDirectory()) {
                    file.mkdirs()
                } else {
                    file.createNewFile()
                    file.withOutputStream { os ->
                        int read
                        while ((read = zipStream.read(buff)) != -1) {
                            os.write(buff, 0, read)
                        }

                    }
                }
            }
        }
    }

    static Map<String, byte[]> unzip(byte[] zip) {
        Map<String, byte[]> result = [:]
        def zipStream = new ZipInputStream(new ByteArrayInputStream(zip))
        ZipEntry entry
        while ((entry = zipStream.getNextEntry()) != null) {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int bytesRead;
            final byte[] buffer = new byte[1024];
            while ((bytesRead = zipStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            result.put(entry.getName(), byteArrayOutputStream.toByteArray())
        }
        result
    }

    static byte[] zip(Map<String, byte[]> content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        new ZipOutputStream(out).withStream { zip ->
            content.each { entry->
                zip.with {
                    putNextEntry(new ZipEntry(entry.key))
                    write(entry.value)
                    flush()
                    closeEntry()
                }
            }
        }
        return out.toByteArray()
    }

}
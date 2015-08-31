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
import java.util.zip.ZipInputStream

/**
 *
 * Util classes that contains common methods for I/O
 *
 * @author Celine Souchet
 *
 */
public class IOUtil {

    private final static int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public final static String LINE_SEPARATOR = System.getProperty("line.separator");

    public static read = System.in.newReader().&readLine

    public final static String AUTO_ACCEPT = "auto.accept"

    public static boolean isAutoAccept() {
        return System.getProperty(AUTO_ACCEPT) == "true"
    }
    /**
     *
     *  Wrap the system out with ' | ' when executing the closure
     */
    public static void executeWrappedWithTabs(Closure closure) {
        def stdout = System.out;
        System.setOut(new PrintStream(stdout) {
            @Override
            public void println(String x) {
                stdout.print(" | ")
                stdout.println(x)
            }
        })
        closure.call()
        System.setOut(stdout);
    }

    public static void deleteDirectory(File dir) {
        if (dir == null) {
            throw new IllegalArgumentException("Can't execute migrateDirectory method with arguments : dir = " + dir);
        }

        println "Replacing all content of $dir..."

        if (!dir.deleteDir()) {
            throw new IllegalStateException("Migration failed. Unable to delete : " + dir)
        }
    }

    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            destDir.setLastModified(srcDir.lastModified());
            if (!destDir.canWrite()) {
                throw new IOException("Destination '" + destDir + "' cannot be written to");
            }
        }
        // recurse
        File[] files = srcDir.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }
        for (int i = 0; i < files.length; i++) {
            File copiedFile = new File(destDir, files[i].getName());
            if (files[i].isDirectory()) {
                copyDirectory(files[i], copiedFile);
            } else {
                copyFile(files[i], copiedFile);
            }
        }
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        if (destFile.exists() && !(destFile.delete())) {
            throw new IllegalStateException("Migration failed. Unable to delete : " + destFile)
        }
        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                long count = 0;
                int n = 0;
                while (-1 != (n = input.read(buffer))) {
                    output.write(buffer, 0, n);
                    count += n;
                }
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignored) {
                    // ignore
                }
            }
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
                // ignore
            }
        }
        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "'");
        }
        destFile.setLastModified(srcFile.lastModified());
    }

    public static Object deserialize(byte[] bytes, ClassLoader theClassLoader) {
        //had to override the method of objectinputstream to be able to give the object classloader in input
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                return Class.forName(objectStreamClass.getName(), true, theClassLoader);
            }
        };
        try {
            return input.readObject();
        } finally {
            input.close();
        }
    }

    public static byte[] serialize(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(object);
            out.flush();
        } finally {
            out.close();
        }
        return baos.toByteArray();
    }

    static void printInRectangle(String... lines) {
        def maxSize = lines.collect { it.size() }.max() + 2
        printLine(maxSize)
        lines.each {
            int spaces = maxSize - it.size()
            print "|"
            printSpaces((int) (spaces / 2))
            print it
            printSpaces(((int) (spaces / 2)) + spaces % 2)
            print "|"
            print LINE_SEPARATOR
        }
        printLine(maxSize)
    }

    static printSpaces(int size) {
        int i = 0;
        while (i < size) {
            i++;
            print ' '
        }
    }

    static printLine(int size) {
        print '+'
        int i = 0;
        while (i < size) {
            i++;
            print '-'
        }
        print '+'
        print LINE_SEPARATOR
    }


    public static void askIfWeContinue() {
        if (!isAutoAccept()) {
            println "Continue migration? (yes/no): "
            def String input = read();
            if (input != "yes") {
                println "Migration cancelled"
                System.exit(0);
            }
        }
    }
    public static void unzip(InputStream inputStream, File outputDirectory) {

        byte[] buff = new byte[1024]
        inputStream.withStream { stream ->
            println "bonita home zip = " + stream

            def zipStream = new ZipInputStream(stream)
            def ZipEntry entry
            while ((entry = zipStream.getNextEntry()) != null) {
                def file = new File(outputDirectory, entry.getName())
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.createNewFile()
                    file.withOutputStream { os ->
                        int read
                        while ((read = zipStream.read(buff)) != -1) {
                            os.write(buff, 0, read);
                        }

                    }
                }
            }
        }
    }

}
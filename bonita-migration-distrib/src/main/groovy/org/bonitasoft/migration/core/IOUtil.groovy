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

import groovy.lang.Closure;
import groovy.sql.Sql
import groovy.time.TimeCategory

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.PrintStream;
import java.sql.ResultSet

import org.bonitasoft.migration.core.exception.MigrationException
import org.bonitasoft.migration.core.exception.NotFoundException


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
    
    /**
     *
     *  Wrap the system out with ' | ' when executing the closure
     */
    public static void executeWrappedWithTabs(Closure closure){
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(stdout){
                    @Override
                    public void println(String x) {
                        stdout.print(" | ")
                        stdout.println(x)
                    }
                })
        closure.call()
        System.setOut(stdout);
    }
    
    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        if (!destDir.exists()){
            if (destDir.mkdirs() == false) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            destDir.setLastModified(srcDir.lastModified());
            if (destDir.canWrite() == false) {
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
                } catch (IOException ioe) {
                    // ignore
                }
            }
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
            srcFile + "' to '" + destFile + "'");
        }
        destFile.setLastModified(srcFile.lastModified());
    }

   public static Object deserialize(byte[] bytes, ClassLoader theClassLoader){
        //had to override the method of objectinputstream to be able to give the object classloader in input
        ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes)){
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

    public static byte[] serialize(Object object){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        out = new ObjectOutputStream(baos);
        out.writeObject(object);
        out.flush();
        return baos.toByteArray();
    }
    
    static void printInRectangle(String... lines){
        def maxSize = lines.collect{ it.size() }.max() +2
        printLine(maxSize)
        lines.each {
            int spaces = maxSize - it.size()
            print "|"
            printSpaces((int)(spaces/2))
            print it
            printSpaces(((int)(spaces/2)) + spaces%2)
            print "|"
            print LINE_SEPARATOR
        }
        printLine(maxSize)
    }

    static printSpaces(int size){
        int i = 0;
        while (i<size) {
            i++;
            print ' '
        }
    }
    
    static printLine(int size){
        print '+'
        int i = 0;
        while (i<size) {
            i++;
            print '-'
        }
        print '+'
        print LINE_SEPARATOR
    }
}

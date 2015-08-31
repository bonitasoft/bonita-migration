/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

/**
 * print to standard output + in a file
 *
 * @author Baptiste Mesta
 */
class SplitPrintStream extends OutputStream {

    private final OutputStream out1
    private final OutputStream out2

    public SplitPrintStream(OutputStream out1, OutputStream out2) {
        this.out2 = out2
        this.out1 = out1
    }

    @Override
    public void write(int b) throws IOException {
        out1.write(b)
        out2.write(b)
    }

    @Override
    public void flush() throws IOException {
        out1.flush();
        out2.flush();
    }

    @Override
    public void close() throws IOException {
        out1.close()
        out2.close()
    }
}

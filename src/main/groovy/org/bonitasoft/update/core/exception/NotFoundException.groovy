/**
 * Copyright (C) 2013 Bonitasoft S.A.
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
package org.bonitasoft.update.core.exception

/**
 * @author Celine Souchet
 */
public class NotFoundException extends Exception {

    private static final long serialVersionUID = -1056166500737611443L

    public NotFoundException(final Throwable cause) {
        super(cause)
    }

    public NotFoundException(final String message) {
        super(message)
    }

    public NotFoundException(final String message, final Throwable cause) {
        super(message, cause)
    }
}

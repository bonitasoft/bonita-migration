/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.update.version.to7_13_0

import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Copied from studio-sp Strings.java class
 */
class Strings {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]")
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]")
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-${'$'})")

    static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty()
    }

    /**
     * @param string
     * @return true if the string has non blank text
     */
    static boolean hasText(String string) {
        return string != null && !string.isBlank()
    }

    static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"),
                " ")
                .replace("_", " ")
                .replace("-", " ")
                .replace("   ", " ")
                .replace("  ", " ")
    }

    static String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-")
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
        String slug = NONLATIN.matcher(normalized).replaceAll("")
        slug = EDGESDHASHES.matcher(slug).replaceAll("")
        return slug.toLowerCase(Locale.ENGLISH)
    }

    static String toKebabCase(String input) {
        return slugify(splitCamelCase(input).toLowerCase())
    }
}

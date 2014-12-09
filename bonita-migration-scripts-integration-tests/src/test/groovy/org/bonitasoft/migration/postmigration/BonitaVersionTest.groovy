/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.migration.postmigration

import org.junit.Test
import static org.assertj.core.api.Assertions.assertThat

/**
 * @author Emmanuel Duchastenier
 *
 */
class BonitaVersionTest {

    @Test
    public void getNumericVersionShouldConvertToNumeric() {
        assertThat(PostMigrationTest.getCurrentNumericVersion("6.3.9")).isEqualTo(639);
    }

    @Test
    public void getNumericVersionShouldReturnFirst3Digits() {
        assertThat(PostMigrationTest.getCurrentNumericVersion("6.4.2-toto")).isEqualTo(642);
    }


    @Test
    public void getNumericVersionShouldRemoveDots() {
        assertThat(PostMigrationTest.getCurrentNumericVersion("6..4.0")).isEqualTo(640);
    }

}

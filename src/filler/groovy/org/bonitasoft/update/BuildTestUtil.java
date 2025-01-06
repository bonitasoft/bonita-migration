/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.update;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.io.IOUtil;

public class BuildTestUtil {

    public static BusinessArchiveBuilder buildBusinessArchiveWithConnectorAndUserFilter(
            final ProcessDefinitionBuilder processDefinitionBuilder,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final List<BarResource> userFilters)
            throws InvalidProcessDefinitionException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(
                        processDefinitionBuilder.done());
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        for (final BarResource barResource : userFilters) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        return businessArchiveBuilder;
    }

    public static BarResource generateJarAndBuildBarResource(final Class<?> clazz, final String name)
            throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        return new BarResource(name, data);
    }

    public static BarResource getContentAndBuildBarResource(final String name, final Class<? extends Connector> clazz)
            throws IOException {
        final InputStream stream = clazz.getResourceAsStream(name);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource(name, byteArray);
    }

}

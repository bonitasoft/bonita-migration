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
 */
package org.bonitasoft.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.page.Page;
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class DatabaseChecker7_0_2 extends SimpleDatabaseChecker7_0_0 {

    public static final String README_NAME = "README.txt";
    private static String UTF8 = "UTF-8";

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker7_0_2.class.getName());
    }

    @Test
    public void should_update_default_theme_content() throws Exception {
        Page page = getPageAPI().getPageByName(ApplicationService.DEFAULT_THEME_NAME);
        assertThat(page).isNotNull();
        byte[] pageContent = getPageAPI().getPageContent(page.getId());
        assertThat(pageContent).isNotNull();

        Properties properties = loadReadme(pageContent);
        assertThat(properties.getProperty("name")).isEqualTo("custompage_bootstrapdefaulttheme");
        assertThat(properties.getProperty("displayName")).isEqualTo("Bootstrap default theme");
        assertThat(properties.getProperty("description")).isEqualTo("Application theme based on bootstrap \"Default\" theme. (see http://bootswatch.com/default/)");
        assertThat(properties.getProperty("contentType")).isEqualTo("theme");
    }

    private Properties loadReadme(final byte[] content) throws Exception {
        String pagePropertiesContent = retrievePagePropertiesContent(content);
        final Properties props = new Properties();
        try (StringReader reader = new StringReader(pagePropertiesContent)) {
            props.load(reader);
        }

        return props;
    }

    private final String retrievePagePropertiesContent(final byte[] zipFile) throws Exception {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(zipFile);
                final ZipInputStream zipInputstream = new ZipInputStream(bais)) {

            String content = null;
            ZipEntry pageProperties = findReadmeEntry(zipInputstream);
            if (pageProperties != null) {
                content = readContent(zipInputstream);
            }
            return content;
        }
    }

    private String readContent(final ZipInputStream zipInputstream) throws IOException {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            int bytesRead;
            final byte[] buffer = new byte[4096];
            while ((bytesRead = zipInputstream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return new String(byteArrayOutputStream.toByteArray(), UTF8);
        }
    }

    private ZipEntry findReadmeEntry(final ZipInputStream zipInputstream) throws IOException {
        ZipEntry pageProperties;
        do {
            pageProperties = zipInputstream.getNextEntry();
        } while (pageProperties != null && !pageProperties.getName().equals(README_NAME));
        return pageProperties;
    }

}

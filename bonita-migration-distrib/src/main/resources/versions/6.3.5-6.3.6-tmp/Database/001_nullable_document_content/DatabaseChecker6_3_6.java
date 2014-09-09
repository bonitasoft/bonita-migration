/**
 * Copyright (C) 214 BonitaSoft S.A.
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

import org.junit.runner.JUnitCore;

import java.util.List;

public class DatabaseChecker6_3_6 extends SimpleDatabaseChecker6_3_4 {

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_3_6.class.getName());
    }

    @Test
    public void should_allow_documents_with_null_content() throws Exception {
        ProcessInstance instance = processAPI.startProcess(processAPI.getProcessDefinitionId(
                SimpleDatabaseFiller6_0_2.PROCESS_NAME,
                SimpleDatabaseFiller6_0_2.PROCESS_VERSION));

        Document document = processAPI.attachDocument(
                instance.getId(),
                "Document Name",
                "File Name",
                "Mime",
                (byte[]) null);

        assertThat(processAPI.getDocumentContent(document.getContentStorageId())).isNull();

        processAPI.deleteProcessInstance(instance.getId());
    }
}

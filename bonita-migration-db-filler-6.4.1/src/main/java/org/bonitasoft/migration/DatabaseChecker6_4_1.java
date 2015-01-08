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
package org.bonitasoft.migration;

import static org.assertj.core.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatabaseChecker6_4_1 extends SimpleDatabaseChecker6_4_0 {

    private static Logger logger = LoggerFactory.getLogger(DatabaseChecker6_4_1.class);

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_4_1.class.getName());
    }

    @Test
    public void checkDates_are_retrieved_Correctly() throws Exception {
        logger.info("start checking date migration!!!!");
        final SearchResult<ArchivedProcessInstance> archivedProcessInstances = processAPI.searchArchivedProcessInstances(new SearchOptionsBuilder(0, 100)
                .filter(ArchivedProcessInstancesSearchDescriptor.NAME, "ArchivedDateDataVariableProcessToBeMigrated")
        .sort(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, Order.ASC).done());
        assertThat(archivedProcessInstances.getCount()).isEqualTo(10);
        for (final ArchivedProcessInstance aprocInstance : archivedProcessInstances.getResult()) {
            final List<ArchivedDataInstance> aDataList = processAPI.getArchivedProcessDataInstances(aprocInstance.getSourceObjectId(), 0, 100);
            assertThat(aDataList).hasSize(1);

            logger.info("\n\n\n\n***********************************");
            logger.info("{}", extractProperty("value", Date.class).from(aDataList));
            for (final Date extractedDate : extractProperty("value", Date.class).from(aDataList)) {
                if (extractedDate != null) {
                    logger.info("{} {}", extractedDate, extractedDate.getTime());
                }
            }
            logger.info("***********************************\n\n\n\n");
            assertThat(extractProperty("name", String.class).from(aDataList)).hasSize(2).contains("dateData", "nullDateData");//.get(0).getName()).isEqualTo("dateData");
            assertThat(extractProperty("value", Date.class).from(aDataList)).hasSize(2).containsNull();//.get(0).getName()).isEqualTo("dateData");

        }

        final SearchResult<ProcessInstance> processInstances = processAPI.searchProcessInstances(new SearchOptionsBuilder(0, 100)
        .filter(ProcessInstanceSearchDescriptor.NAME, "DateDataVariableProcessToBeMigrated")
        .sort(ProcessInstanceSearchDescriptor.ID, Order.ASC).done());
        assertThat(processInstances.getCount()).isEqualTo(10);
        for (final ProcessInstance procInstance : processInstances.getResult()) {
            final List<DataInstance> dataList = processAPI.getProcessDataInstances(procInstance.getId(), 0, 100);
            logger.info("\n\n\n\n***********************************");
            logger.info("{}", extractProperty("value", Date.class).from(dataList));
            for (final Date extractedDate : extractProperty("value", Date.class).from(dataList)) {
                if (extractedDate != null) {
                    logger.info("{} {}", extractedDate, extractedDate.getTime());
                }
            }
            logger.info("***********************************\n\n\n\n");
            assertThat(dataList).hasSize(2);
            assertThat(extractProperty("name", String.class).from(dataList)).hasSize(2).contains("dateData", "nullDateData");//.get(0).getName()).isEqualTo("dateData");
            assertThat(extractProperty("value", Date.class).from(dataList)).hasSize(2).containsNull();//.get(0).getName()).isEqualTo("dateData");
                if (extractedDate != null) {
                    assertThat(extractedDate.toString()).isEqualTo("Thu Jul 18 14:49:26 CEST 2013");
                }
            }
        }
        logger.info("end checking date migration!!!!");
    }
}

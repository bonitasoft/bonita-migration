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
package org.bonitasoft.migration;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.bonitasoft.engine.exception.BonitaException;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseChecker6_4_0 extends SimpleDatabaseChecker6_3_2 {

    private static final String KIND = "012345678912345";
    private static final String CLASSNAME = "org.bonitasoft.classname";
    private static final int PROCESS_INSTANCE_ID1 = 10000;
    private static final int PROCESS_INSTANCE_ID2 = 10001;

    private static final int FLOWNODE_INSTANCE_ID1 = 10000;
    private static final int FLOWNODE_INSTANCE_ID2 = 10001;

    private static final String SQL_INSERT_PROCESS_INSTANCE = "INSERT INTO process_instance(tenantid, id, name, processdefinitionid, description, startdate, startedby, startedbysubstitute,"
            + " enddate, stateid, statecategory, lastupdate, containerid, rootprocessinstanceid, callerid, callertype, interruptingeventid,"
            + " migration_plan, stringindex1, stringindex2, stringindex3, stringindex4, stringindex5)"
            + " VALUES(?, ?, 'name', 0, 'desc', 0, 0, 0, 0, 0, 'a', 0, 0, 0, 0, 'b', 0, 0, 'c', 'd', 'e', 'f', 'g')";

    private static final String SQL_INSERT_FLOWNODE = "INSERT INTO flownode_instance(tenantid, id, flownodedefinitionid, kind, rootcontainerid, parentcontainerid, name, displayname,"
            + " displaydescription, stateid, statename, prev_state_id, terminal, stable, actorid, assigneeid, reachedstatedate, lastupdatedate, expectedenddate, claimeddate,"
            + " priority, gatewaytype, hitbys, statecategory, logicalgroup1, logicalgroup2, logicalgroup3, logicalgroup4, loop_counter, loop_max, description, sequential,"
            + " loopdatainputref, loopdataoutputref, datainputitemref, dataoutputitemref, loopcardinality, nbactiveinst, nbcompletedinst, nbterminatedinst, executedby,"
            + " executedbysubstitute, activityinstanceid, state_executing, abortedbyboundary, triggeredbyevent, interrupting, deleted, tokencount, token_ref_id)"
            + " VALUES(?, ?, 0, ?, 0, 0, 'name', 'a', 'b', 0, 'c', 0, ?, ?, 0, 0, 0, 0, 0, 0, 0, 'd', 'e', 'f', 0, 0, 0, 0, 0, 0, 'g', ?, 'h', 'i', 'j', 'k',"
            + " 0, 0, 0, 0, 0, 0, 0, ?, 0, ?, ?, ?, 0, 0)";

    private static final String SQL_INSERT_TENANT = " INSERT INTO tenant(id, created, createdby, description, defaulttenant, iconname, iconpath, name, status)"
            + " VALUES(?, 0, 'a', 'b', ?, 'c', 'd', 'e', 'f')";

    private static final int TENANT_ID = 4567;

    private static Logger logger = LoggerFactory.getLogger(DatabaseChecker6_4_0.class);

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_4_0.class.getName());
    }

    @AfterClass
    public static void teardown() throws BonitaException {
        final DataSource bonitaDatasource = (DataSource) springContext.getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        //clean up
        jdbcTemplate.update("DELETE FROM multi_biz_data where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM ref_biz_data_inst where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM flownode_instance where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM process_instance where tenantid = ?", new Object[] { TENANT_ID });
        jdbcTemplate.update("DELETE FROM tenant where id = ?", new Object[] { TENANT_ID });
    }

    @Test
    public void kind_field_has_been_created() throws Exception {
        logger.info("check field kind is present in table ref_biz_data_inst");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        //given
        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);
        assertEquals(0, countMultiBusinessdata(jdbcTemplate));

        //when
        jdbcTemplate.update("INSERT INTO ref_biz_data_inst(tenantid, id, name, data_id, data_classname, kind) "
                + " VALUES (?, ?, ?, ?, ?, ?) ", new Object[] { TENANT_ID, 12020, "businessdata", 1, CLASSNAME, KIND });

        //then
        assertEquals(countRefBusinessdata + 1, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        emptyRefBizDataTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));
    }

    @Test
    public void ref_biz_data_inst_flownode_id_check() throws Exception {
        logger.info("check nullable fields on table ref_biz_data_inst");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        //given
        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);

        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        jdbcTemplate.update(SQL_INSERT_PROCESS_INSTANCE, new Object[] { TENANT_ID, PROCESS_INSTANCE_ID1 });
        jdbcTemplate.update(SQL_INSERT_PROCESS_INSTANCE, new Object[] { TENANT_ID, PROCESS_INSTANCE_ID2 });

        //when
        final String sqlInsertRefBizData = "INSERT INTO ref_biz_data_inst(tenantid, id, name, proc_inst_id, fn_inst_id, data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlInsertRefBizData, new Object[] { TENANT_ID, 38484, "businessdata", PROCESS_INSTANCE_ID1, null, 1, CLASSNAME,
                KIND });

        jdbcTemplate.update(sqlInsertRefBizData, new Object[] { TENANT_ID, 4959595, "businessdata", PROCESS_INSTANCE_ID2, null, 1, CLASSNAME,
                KIND
        });

        //then
        assertEquals(countRefBusinessdata + 2, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        emptyProcessTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));

    }

    @Test
    public void flownode_fk_constraint_check() throws Exception {
        logger.info("check nullable fields on table ref_biz_data_inst");
        final DataSource bonitaDatasource = (DataSource) springContext.getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        //given
        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);

        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        jdbcTemplate
        .update(SQL_INSERT_FLOWNODE
                , new Object[] { TENANT_ID, FLOWNODE_INSTANCE_ID1, "kind", false, false, false, false, false, false, false });

        jdbcTemplate
        .update(SQL_INSERT_FLOWNODE
                , new Object[] { TENANT_ID, FLOWNODE_INSTANCE_ID2, "kind", false, false, false, false, false, false, false });

        //when

        final String sqlInsertRefBizzData = "INSERT INTO ref_biz_data_inst(tenantid, id, name, proc_inst_id, fn_inst_id, data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlInsertRefBizzData, new Object[] { TENANT_ID, 45678, "businessdata", null, FLOWNODE_INSTANCE_ID1, 1, CLASSNAME,
                KIND });

        jdbcTemplate.update(sqlInsertRefBizzData, new Object[] { TENANT_ID, 8754, "businessdata", null, FLOWNODE_INSTANCE_ID2, 1, CLASSNAME,
                KIND });

        //then
        assertEquals(countRefBusinessdata + 2, countRefBusinessdata(jdbcTemplate, TENANT_ID));

        //cleanup
        emptyFlowNodeTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));

    }

    @Test
    public void new_table_has_been_created() throws Exception {
        logger.info("check table multi_biz_data");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);

        final long countRefBusinessdata = countRefBusinessdata(jdbcTemplate, TENANT_ID);
        assertEquals(0, countMultiBusinessdata(jdbcTemplate));

        createTenantIfNotExists(jdbcTemplate, TENANT_ID);
        jdbcTemplate.update("INSERT INTO ref_biz_data_inst(tenantid, id, name,  data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?)", new Object[] { TENANT_ID, 298989, "businessdata", 1, CLASSNAME, "multi_ref" });
        logger.info("insert first multiple data");
        jdbcTemplate.update("INSERT INTO multi_biz_data(tenantid, id, idx, data_id) "
                + "VALUES (?, ?, ?, ?)", new Object[] { TENANT_ID, 298989, 1, 1 });
        logger.info("insert second multiple data");
        jdbcTemplate.update("INSERT INTO multi_biz_data(tenantid, id, idx, data_id) "
                + "VALUES (?, ?, ?, ?)", new Object[] { TENANT_ID, 298989, 2, 2 });

        //then
        assertEquals(countRefBusinessdata + 1, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        assertEquals(2, countMultiBusinessdata(jdbcTemplate));
        logger.info("check delete cascade works");
        emptyRefBizDataTable(jdbcTemplate);
        assertEquals(countRefBusinessdata, countRefBusinessdata(jdbcTemplate, TENANT_ID));
        assertEquals(0, countMultiBusinessdata(jdbcTemplate));
    }

    private void emptyRefBizDataTable(final JdbcTemplate jdbcTemplate) {
        logger.info("clean table ref_biz_data_inst");
        jdbcTemplate.update("DELETE FROM ref_biz_data_inst where tenantid = ?", new Object[] { TENANT_ID });
    }

    private void emptyProcessTable(final JdbcTemplate jdbcTemplate) {
        logger.info("clean table process_instance");
        jdbcTemplate.update("DELETE FROM process_instance where tenantid = ?", new Object[] { TENANT_ID });
    }

    private void emptyFlowNodeTable(final JdbcTemplate jdbcTemplate) {
        logger.info("clean table ref_biz_data_inst");
        jdbcTemplate.update("DELETE FROM flownode_instance where tenantid = ?", new Object[] { TENANT_ID });
    }

    private long countRefBusinessdata(final JdbcTemplate jdbcTemplate, final long tenantId) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM ref_biz_data_inst where tenantid=" + tenantId);
    }

    private long countTenant(final JdbcTemplate jdbcTemplate, final long tenantId) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM tenant where id=" + tenantId);
    }

    private long countMultiBusinessdata(final JdbcTemplate jdbcTemplate) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM multi_biz_data");
    }

    private long getCount(final JdbcTemplate jdbcTemplate, final String sql) {
        final long count = jdbcTemplate.queryForLong(sql);
        logger.info("getCount:" + sql + ":" + count);
        return count;
    }

    private void createTenantIfNotExists(final JdbcTemplate jdbcTemplate, final long tenantId) {
        if (countTenant(jdbcTemplate, tenantId) == 0)
        {
            jdbcTemplate.update(SQL_INSERT_TENANT, new Object[] { tenantId, false });
        }
    }

}

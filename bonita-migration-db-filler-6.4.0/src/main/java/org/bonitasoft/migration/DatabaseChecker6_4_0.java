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

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseChecker6_4_0 extends SimpleDatabaseChecker6_3_2 {

    private static Logger logger = LoggerFactory.getLogger(DatabaseChecker6_4_0.class);

    public static void main(final String[] args) throws Exception {
        JUnitCore.main(DatabaseChecker6_4_0.class.getName());
    }

    @Test
    public void new_field_has_been_created() throws Exception {
        logger.info("check field kind is present in table ref_biz_data_inst");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);
        jdbcTemplate.update("INSERT INTO ref_biz_data_inst(tenantid, id, name, proc_inst_id, data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)", new Object[] { 1, 1, "businessdata", 1, 1, "org.bonitasoft.classname", "simple_ref" });
        assertEquals(1, countRefBusinessdata(jdbcTemplate));
        emptyRefBizDataTable(jdbcTemplate);
    }

    @Test
    public void new_table_has_been_created() throws Exception {
        logger.info("check table multi_biz_data");
        final DataSource bonitaDatasource = (DataSource) getSpringContext().getBean("bonitaDataSource");
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(bonitaDatasource);
        jdbcTemplate.update("INSERT INTO ref_biz_data_inst(tenantid, id, name, proc_inst_id, data_id, data_classname, kind) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)", new Object[] { 1, 2, "businessdata", 1, 1, "org.bonitasoft.classname", "multi_ref" });
        logger.info("insert first multiple data");
        jdbcTemplate.update("INSERT INTO multi_biz_data(tenantid, id, idx, data_id) "
                + "VALUES (?, ?, ?, ?)", new Object[] { 1, 2, 1, 1 });
        logger.info("insert second multiple data");
        jdbcTemplate.update("INSERT INTO multi_biz_data(tenantid, id, idx, data_id) "
                + "VALUES (?, ?, ?, ?)", new Object[] { 1, 2, 2, 2 });
        assertEquals(1, countRefBusinessdata(jdbcTemplate));
        assertEquals(2, countMultiBusinessdata(jdbcTemplate));
        logger.info("check delete cascade works");
        emptyRefBizDataTable(jdbcTemplate);
        assertEquals(0, countRefBusinessdata(jdbcTemplate));
        assertEquals(0, countMultiBusinessdata(jdbcTemplate));
    }

    private void emptyRefBizDataTable(final JdbcTemplate jdbcTemplate) {
        logger.info("clean table ref_biz_data_inst");
        jdbcTemplate.update("DELETE FROM ref_biz_data_inst where tenantid = ?", new Object[] { 1 });
    }

    private long countRefBusinessdata(final JdbcTemplate jdbcTemplate) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM ref_biz_data_inst");
    }

    private long countMultiBusinessdata(final JdbcTemplate jdbcTemplate) {
        return getCount(jdbcTemplate, "SELECT COUNT(id) FROM multi_biz_data");
    }

    private long getCount(final JdbcTemplate jdbcTemplate, final String sql) {
        final long count = jdbcTemplate.queryForLong(sql);
        logger.info("getCount:" + sql + ":" + count);
        return count;
    }

}

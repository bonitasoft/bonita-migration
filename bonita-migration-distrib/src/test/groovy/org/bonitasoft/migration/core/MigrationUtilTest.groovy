package org.bonitasoft.migration.core;

import static org.junit.Assert.*

import org.gmock.GMockTestCase
import org.junit.Test

class MigrationUtilTest extends GMockTestCase {

	def mockSql

	protected void setUp() throws Exception {
		mockSql = mock(groovy.sql.Sql.class);
	}


	@Test
	public void testParseOrAskArgs() throws Exception {
		def resultMap =  MigrationUtil.parseOrAskArgs("--key1","value1","--key2","value2")

		assertTrue(["key1":"value1","key2":"value2"].equals(resultMap));
	}

//	@Test
//	public void testExecuteDefaultSqlFile(){
//		play {
//			mockSql.load("withTransaction").returns("apple")
//			MigrationUtil.executeDefaultSqlFile( file, dbVendor, sql)
//		}
//	}
//
//
//	@Test
//	public void testExecuteDefaultSqlNotExistingFile(){
//		play {
//			MigrationUtil.executeDefaultSqlFile( file, dbVendor,  sql)
//		}
//	}
//
//	@Test
//	public void testExecuteDefaultSqlNoFile(){
//		play {
//			MigrationUtil.executeDefaultSqlFile(null, dbVendor, sql)
//		}
//	}
//
//
//	@Test
//	public void testExecuteDefaultSqlFileNotExistingVendor(){
//		play {
//			MigrationUtil.executeDefaultSqlFile(file, "plop", sql)
//		}
//	}
//
//	@Test
//	public void testExecuteDefaultSqlFileNoVendor(){
//		play {
//			MigrationUtil.executeDefaultSqlFile(file, null, sql)
//		}
//	}
//
//	@Test
//	public void testExecuteDefaultSqlFileNoSql(){
//		play {
//			MigrationUtil.executeDefaultSqlFile(file, dbVendor, null)
//		}
//	}
}

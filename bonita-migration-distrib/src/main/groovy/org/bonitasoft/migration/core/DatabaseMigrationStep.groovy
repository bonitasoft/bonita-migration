/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.migration.core

import groovy.sql.Sql

/**
 *
 * SQL methods are based on postgres and we replace for the current db vendor
 *
 * @author Baptiste Mesta
 */
abstract class DatabaseMigrationStep {

    Sql sql
    String dbVendor

    DatabaseMigrationStep(Sql sql, String dbVendor) {
        this.dbVendor = dbVendor
        this.sql = sql
    }

    /**
     * execute a postgres script converted to the database specified by dbVendor
     * @param statement
     * @return
     */
    def boolean execute(GString statement) {
        return sql.execute(adaptFor(statement))
    }

    def boolean execute(String statement) {
        return sql.execute(adaptFor(statement))
    }

    def int executeUpdate(GString statement) {
        return sql.executeUpdate(adaptFor(statement))
    }

    def int executeUpdate(String statement) {
        return sql.executeUpdate(adaptFor(statement))
    }

    /*
     *   Replace all things to work with all bases
     *
     */

    def String adaptFor(String statement) {
        switch (dbVendor) {
            case "oracle":
                return adaptForOracle(statement)
                break;
            case "mysql":
                return adaptForMysql(statement)
                break;
            case "sqlserver":
                return adaptForSqlServer(statement)
                break;
            default:
                return statement
        }
    }

    def static String adaptForOracle(String statement) {
        def oracleStatement = statement;
        oracleStatement = oracleStatement.replaceAll("BYTEA", "BLOB")
        oracleStatement = oracleStatement.replaceAll("BIGINT", "NUMBER(19, 0)")
        oracleStatement = oracleStatement.replaceAll("INT8", "NUMBER(19, 0)")
        oracleStatement = oracleStatement.replaceAll("INTEGER", "NUMBER(10, 0)")
        oracleStatement = oracleStatement.replaceAll("VARCHAR", "VARCHAR2")
        oracleStatement = oracleStatement.replaceAll("TEXT", "VARCHAR2(1024)")
        oracleStatement = oracleStatement.replaceAll("SMALLINT", "SMALLINT")
        oracleStatement = oracleStatement.replaceAll("LONGVARBINARY", "BLOB")
        oracleStatement = oracleStatement.replaceAll("LONGBLOB", "BLOB")
        oracleStatement = oracleStatement.replaceAll("BOOLEAN", " NUMBER(1)")
        oracleStatement = oracleStatement.replaceAll("true", "1")
        oracleStatement = oracleStatement.replaceAll("false", "0")
        return oracleStatement;
    }

    def static String adaptForMysql(String statement) {
        def oracleStatement = statement;
        oracleStatement = oracleStatement.replaceAll("BYTEA", "BLOB")
        oracleStatement = oracleStatement.replaceAll("INT8", "BIGINT")
        return oracleStatement;
    }

    def static String adaptForSqlServer(String statement) {
        def oracleStatement = statement;
        oracleStatement = oracleStatement.replaceAll("BYTEA", "VARBINARY(MAX)")
        oracleStatement = oracleStatement.replaceAll("BLOB", "VARBINARY(MAX)")
        oracleStatement = oracleStatement.replaceAll("BIGINT", "NUMERIC(19, 0)")
        oracleStatement = oracleStatement.replaceAll("INT8", "NUMERIC(19, 0)")
        oracleStatement = oracleStatement.replaceAll("VARCHAR", "NVARCHAR")
        oracleStatement = oracleStatement.replaceAll("TEXT", "NVARCHAR(MAX)")
        oracleStatement = oracleStatement.replaceAll("LONGVARBINARY", "BLOB")
        oracleStatement = oracleStatement.replaceAll("DEFAULT true", "DEFAULT 1")
        oracleStatement = oracleStatement.replaceAll("DEFAULT TRUE", "DEFAULT 1")
        oracleStatement = oracleStatement.replaceAll("DEFAULT false", "DEFAULT 0")
        oracleStatement = oracleStatement.replaceAll("DEFAULT FALSE", "DEFAULT 0")
        oracleStatement = oracleStatement.replaceAll("BOOLEAN", " BIT")
        oracleStatement = oracleStatement.replaceAll(";", "\nGO")
        return oracleStatement;
    }

    def renameColumn(String table, String oldName, String newName, String newType) {
        switch (dbVendor) {
            case "oracle":
                execute("ALTER TABLE $table RENAME COLUMN $oldName TO $newName")
                break;
            case "mysql":
                execute("ALTER TABLE $table CHANGE COLUMN $oldName $newName $newType")
                break;
            case "sqlserver":
                execute("""BEGIN
EXEC sp_rename '${table}.$oldName', '$newName', 'COLUMN'
END""")
                break;
            default:
                execute("ALTER TABLE $table RENAME $oldName TO $newName")
        }
    }

    def renameTable(String table, String newName) {
        switch (dbVendor) {
            case "mysql":
                execute("RENAME TABLE $table TO $newName")
                break;
            case "sqlserver":
                execute("sp_rename $table , $newName")
                break;
            default:
                execute("ALTER TABLE $table RENAME TO $newName")
        }
    }

    def dropNotNull(String table, String column, String type) {
        switch (dbVendor) {
            case "oracle":
                execute("ALTER TABLE $table MODIFY $column NULL")
                break;
            case "mysql":
                execute("ALTER TABLE $table MODIFY $column $type NULL")
                break;
            case "sqlserver":
                execute("ALTER TABLE $table ALTER COLUMN $column $type NULL")
                break;
            default:
                execute("ALTER TABLE $table ALTER COLUMN $column DROP NOT NULL")
        }
    }

    def dropColumn(String table, String column) {
        switch (dbVendor) {
            case "oracle":
                execute("ALTER TABLE $table DROP COLUMN $column")
                break;
            case "sqlserver":
                execute("ALTER TABLE $table DROP COLUMN $column")
                break;
            default:
                execute("ALTER TABLE $table DROP $column")
        }
    }

    def addColumn(String table, String column, String type, String defaultValue, String constraint) {
        execute("ALTER TABLE $table ADD $column $type ${defaultValue != null ? "DEFAULT $defaultValue" : ""} ${constraint != null ? constraint : ""}")
    }

    def dropForeignKey(String table, String name) {
        switch (dbVendor) {
            case "mysql":
                execute("ALTER TABLE $table DROP FOREIGN KEY $name")
                break;
            default:
                execute("ALTER TABLE $table DROP CONSTRAINT $name")
        }
    }
/*
    def addForeignKey(String table, String name, String[] columns, String otherTables, String[] otherColumns, boolean onDeleteCascade) {
        switch (dbVendor) {
            case "mysql":
                execute("ALTER TABLE $table DROP FOREIGN KEY $name")
                break;
            default:
                execute("ALTER TABLE $table ADD CONSTRAINT $name FOREIGN KEY ${parenthesize(columns)} REFERENCES ${otherTables}${parenthesize(otherColumns)} ${onDeleteCascade?"ON DELETE CASCADE":""}")

        }
    }

    def String parenthesize(String[] list){
        def res= "(";
        list.eachWithIndex { String entry, int i ->
            res += "entry"
            if(i<list.size()-1){
                res += ", "
            }
        }
        return res + ")"
    }*/

    public abstract migrate();

}

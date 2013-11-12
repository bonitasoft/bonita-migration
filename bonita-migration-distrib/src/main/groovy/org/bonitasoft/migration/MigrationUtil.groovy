package org.bonitasoft.migration

import groovy.sql.Sql


public class MigrationUtil {


    public static String DB_URL = "db.url";

    public static String DB_USER = "db.user";

    public static String DB_PASSWORD = "db.password";

    public static String DB_DRIVERCLASS = "db.driverclass";

    public static String DB_VENDOR = "db.vendor";



    public Map parseOrAskArgs(String[] args){
        //will ask for missing parameter
        return listToMap(args);
    }


    public Map listToMap( String[] list){
        def map = [:];
        def iterator = list.iterator()
        while (iterator.hasNext()) {
            map.put(iterator.next().substring(2),iterator.next());
        }
        return map;
    }


    public Sql getSqlConnection(Map props){
        return Sql.newInstance(props.get(MigrationUtil.DB_URL), props.get(MigrationUtil.DB_USER), props.get(MigrationUtil.DB_PASSWORD), props.get(MigrationUtil.DB_DRIVERCLASS));
    }
}

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
        def dburl = props.get(MigrationUtil.DB_URL);
        def user = props.get(MigrationUtil.DB_USER);
        def pass = props.get(MigrationUtil.DB_PASSWORD);
        def driver = props.get(MigrationUtil.DB_DRIVERCLASS);
        println "url="+dburl;
        println "user="+user;
        println "pass="+pass;
        println "driver="+driver;
        return Sql.newInstance(dburl, user, pass, driver);
    }
}

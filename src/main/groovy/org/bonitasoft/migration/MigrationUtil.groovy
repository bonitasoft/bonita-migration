package org.bonitasoft.migration


public class MigrationUtil {


    public static Map parseOrAskArgs(String[] args){
        //        def username
        //        System.in.withReader {
        //            print  'input: '
        //            username = it.readLine()
        //            println username
        //        }
        ["username":args[1]]
    }
}

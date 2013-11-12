package org.bonitasoft.migration


public class MigrationUtil {


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
}

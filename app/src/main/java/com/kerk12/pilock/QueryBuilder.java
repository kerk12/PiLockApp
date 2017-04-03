package com.kerk12.pilock;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by kgiannakis on 3/4/2017.
 */

public class QueryBuilder {
    private Map<String, String> queryMap;

    public QueryBuilder(Map<String, String> queryMap) {
        this.queryMap = queryMap;
    }

    public String getQuery() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Map.Entry<String,String> entry: queryMap.entrySet()){
            if (first){
                first = false;
            } else {
                sb.append("&");
            }

            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8") + "="+URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return sb.toString();
    }
}

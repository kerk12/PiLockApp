package com.kerk12.pilock;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Class responsible for building Request parameters. Takes a map of the parameters, and outputs them in a URL/request friendly form.
 */
public class QueryBuilder {
    private Map<String, String> queryMap;

    /**
     * Default constructor. Takes in a map of key=value parameters.
     * @param queryMap The Request Parameters.
     */
    public QueryBuilder(Map<String, String> queryMap) {
        this.queryMap = queryMap;
    }

    /**
     * Return a HTTP request friendly parameter string.
     * For example, "mode: gaming, graphics: high" would result in:
     * mode=gaming&graphics=high
     * @return The parameter string.
     * @throws UnsupportedEncodingException
     */
    public String getQuery() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Map.Entry<String,String> entry: queryMap.entrySet()){
            if (first)
                first = false;
            else
                sb.append("&");

            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8") + "="+URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return sb.toString();
    }
}

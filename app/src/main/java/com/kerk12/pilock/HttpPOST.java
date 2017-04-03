package com.kerk12.pilock;

import java.net.URL;
import java.util.Map;

/**
 * Created by kgiannakis on 4/4/2017.
 */

public class HttpPOST {
    private URL url = null;
    private Map<String, String> params = null;
    private String result = null;

    public HttpPOST(URL url, Map<String, String> params, String result) {
        this.url = url;
        this.params = params;
        this.result = result;
    }
    
}

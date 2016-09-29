package com.secqme.util.rest;

import java.net.URL;
import java.net.URI;
import java.util.concurrent.Callable;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 *
 * @author jameskhoo
 */
public class RestGetWorker extends BaseRestWorker implements Callable {

    private GetMethod httpGetMethod;
    String url;

    public RestGetWorker(String URL) {
        super();
        this.url = URL;
    }

    public GetMethod getHttpMethod() {
        if (httpGetMethod == null) {
            httpGetMethod = new GetMethod(url);
        }
        return httpGetMethod;
    }

    public void setHttpGetMethod(GetMethod httpGetMethod) {
        this.httpGetMethod = httpGetMethod;
    }
}

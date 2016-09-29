package com.secqme.util.rest;

import java.util.concurrent.Callable;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PutMethod;

/**
 *
 *
 * @author jameskhoo
 */
public class RestPutWorker extends BaseRestWorker implements Callable {

    PutMethod putMethod = null;
    String url = null;
    String requestBody = null;

    public RestPutWorker(String URL, String requestBody) {
        super();
        url = URL;
        this.requestBody = requestBody;
    }

    @Override
    public HttpMethod getHttpMethod() {
        if (putMethod == null) {
            putMethod = new PutMethod(url);
            putMethod.setRequestBody(requestBody);
        }
        return putMethod;
    }

    public void setPutMethod(PutMethod putMethod) {
        this.putMethod = putMethod;
    }
}

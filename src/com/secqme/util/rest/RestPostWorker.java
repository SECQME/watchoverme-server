package com.secqme.util.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import twitter4j.internal.http.HttpResponse;

/**
 *
 *
 * @author jameskhoo
 */
public class RestPostWorker extends BaseRestWorker implements Callable {

    PostMethod postMethod = null;
    String url = null;
    String requestBody = null;

    public RestPostWorker(String URL, String requestBody) {
        super();
        url = URL;
        this.requestBody = requestBody;
        postMethod = new PostMethod(URL);
        setRequestBody();
    }

    public RestPostWorker(String URL, HashMap<String, String> parameters) {
        super();
        url = URL;
        postMethod = new PostMethod(URL);
        Iterator<Entry<String,String>> i = parameters.entrySet().iterator();
        Entry<String, String> entry;
        while(i.hasNext()) {
            entry = i.next();
            postMethod.addParameter(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public HttpMethod getHttpMethod() {
        if (postMethod == null) {
            postMethod = new PostMethod(url);
            setRequestBody();
        }
        return postMethod;
    }

    public void setPostMethod(PostMethod postMethod) {
        this.postMethod = postMethod;
    }
    
    private void setRequestBody() {
        try {
            StringRequestEntity requestEntity = new StringRequestEntity(
                    requestBody, "application/json", "UTF-8"
            );
            postMethod.setRequestEntity(requestEntity);
        } catch (UnsupportedEncodingException ie) {

        }

    }
    
    
}

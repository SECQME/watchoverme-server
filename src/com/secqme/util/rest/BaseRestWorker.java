package com.secqme.util.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;

/**
 *
 * @author jameskhoo
 */
public abstract class BaseRestWorker implements Callable {

    private static Logger myLog = Logger.getLogger(BaseRestWorker.class);
    protected HttpClient httpClient = null;
    protected HttpClientParams params = null;
    protected Header header = null;
    protected Cookie myCookie = null;
    protected HttpState initialState = null;
    protected Properties clientRequestHeader = null;

    private final static int HTTP_NOT_OK_STATUS_CODE_START = 400;

    public BaseRestWorker() {
        //empty constructor
    }

    public void setHttpClient(HttpClient client) {
        this.httpClient = client;
    }

    public void setClientRequestHeader(Properties headerProperties) {
        this.clientRequestHeader = headerProperties;
    }

    public abstract HttpMethod getHttpMethod();

    protected void initHttpClient() {
        if (httpClient == null) {
            httpClient = new HttpClient();
        }
        params = new HttpClientParams();
        myCookie = new Cookie(".secq.me", "mycookie", "stuff",
                "/", null, false);
        initialState = new HttpState();
        initialState.addCookie(myCookie);
        httpClient.setParams(params);
        httpClient.setState(initialState);
        httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

    }

    /**
     *
     * @return
     * @throws java.lang.Exception
     * TODO needs to handle specific Excepton
     */
    public String call() throws RestExecException {
        String httpResponseString = null;
        HttpMethod method = getHttpMethod();
        // method.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
        // method.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        if (clientRequestHeader != null) {
            for (Iterator it = clientRequestHeader.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                method.setRequestHeader((String) entry.getKey(), (String) entry.getValue());
            }
        }
        int statusCode = 0;
        String URL = null;
        try {
            initHttpClient();
            URL = method.getURI().toString();
            myLog.debug("HttpClient initialized, method:" + method.getName() + ", URL:" + URL);
            statusCode = httpClient.executeMethod(method);
            myLog.debug("REST Method executed, status" + "Code is->" + statusCode);

            if (method.getResponseBodyAsStream() != null) {
                httpResponseString = parseResponse(method.getResponseBodyAsStream());
            }
        } catch (Exception ex) {
            myLog.error(ex.getMessage(), ex);
            throw new RestExecException(statusCode, URL, ex);
        } finally {
            method.releaseConnection();
            if(httpClient !=null) {
                httpClient.getHttpConnectionManager().closeIdleConnections(60000);
            }

        }
        return httpResponseString;
    }

    private String parseResponse(InputStream is) throws IOException {
        BufferedReader in =
                new BufferedReader(new InputStreamReader(is));
        String input;

        StringBuffer strBuf = new StringBuffer();
        while ((input = in.readLine()) != null) {
            strBuf.append(input);
        }
        return strBuf.toString();
    }
}

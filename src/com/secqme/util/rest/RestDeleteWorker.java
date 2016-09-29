package com.secqme.util.rest;

import java.util.concurrent.Callable;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;

/**
 *
 *
 * @author jameskhoo
 */
public class RestDeleteWorker extends BaseRestWorker implements Callable {

    DeleteMethod deleteMethod = null;
    String url = null;

    public RestDeleteWorker(String URL) {
        super();
        this.url = URL;
    }

    @Override
    public HttpMethod getHttpMethod() {
        if (deleteMethod == null) {
            deleteMethod = new DeleteMethod(url);
        }
        return deleteMethod;
    }

    public void setDeleteMethod(DeleteMethod deleteMethod) {
        this.deleteMethod = deleteMethod;
    }
}

package com.secqme.util.rest;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.log4j.Logger;

/**
 *
 * @author jameskhoo
 */
public class DefaultRestUtil implements RestUtil {

    private static final Logger myLog = Logger.getLogger(DefaultRestUtil.class);
    private final static ExecutorService threadPool = Executors.newCachedThreadPool();

    public DefaultRestUtil() {
        myLog.debug("Initializing new ChacheThree Pool for " + DefaultRestUtil.class.getName());
    }

    public String executeGet(String URL, Properties header) throws RestExecException {
        RestGetWorker getWorker = new RestGetWorker(URL);
        getWorker.setClientRequestHeader(header);
        return execute(getWorker);
    }

    public String executePost(String URL, String requestBody, Properties header) throws RestExecException {
        RestPostWorker postWorker = new RestPostWorker(URL, requestBody);
        postWorker.setClientRequestHeader(header);
        return execute(postWorker);
    }

    public String executePost(String URL, HashMap<String, String> parameters, Properties header) throws RestExecException {
        RestPostWorker postWorker = new RestPostWorker(URL, parameters);
        postWorker.setClientRequestHeader(header);
        return execute(postWorker);
    }

    public String executePut(String URL, String requestBody, Properties header) throws RestExecException {
        RestPutWorker putWorker = new RestPutWorker(URL, requestBody);
        putWorker.setClientRequestHeader(header);
        return execute(putWorker);
    }

    public String executeDelete(String URL, Properties header) throws RestExecException {
        RestDeleteWorker deleteWorker = new RestDeleteWorker(URL);
        deleteWorker.setClientRequestHeader(header);
        return execute(deleteWorker);
    }

    private String execute(BaseRestWorker restWorker) throws RestExecException {
        String restResponse = null;
        try {
            FutureTask futureTask = new FutureTask(restWorker);
            threadPool.submit(futureTask);
            restResponse = (String) futureTask.get();
        } catch (InterruptedException ex) {
            myLog.error(ex.getMessage(), ex);
            throw new RestExecException(restWorker.getHttpMethod().getStatusCode(), "InterruptedException occur", ex);
        } catch (ExecutionException ex) {
            myLog.error(ex.getMessage(), ex);
            throw new RestExecException(restWorker.getHttpMethod().getStatusCode(), "ExecutionException occur", ex);
        } catch (Exception ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return restResponse;
    }
}

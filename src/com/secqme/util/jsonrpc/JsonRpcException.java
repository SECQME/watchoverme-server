package com.secqme.util.jsonrpc;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by edward on 20/07/2015.
 */
public class JsonRpcException extends Exception {

    private JSONObject result;

    public JsonRpcException(String message, JSONObject result) {
        super(message);
        this.result = result;
    }

    public JSONObject getResult() {
        return result;
    }

    public void setResult(JSONObject result) {
        this.result = result;
    }
}

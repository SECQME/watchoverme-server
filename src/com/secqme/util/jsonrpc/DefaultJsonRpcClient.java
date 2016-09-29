package com.secqme.util.jsonrpc;

import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.validation.constraints.NotNull;

/**
 * Created by edward on 16/07/2015.
 */
public class DefaultJsonRpcClient implements JsonRpcClient {

    private static final Logger myLog = Logger.getLogger(DefaultJsonRpcClient.class);

    private RestUtil restUtil;
    private String baseUrl;

    public DefaultJsonRpcClient(@NotNull RestUtil restUtil, @NotNull String baseUrl) {
        this.restUtil = restUtil;
        this.baseUrl = baseUrl;
    }

    @Override
    public JSONObject call(@NotNull String method, @NotNull JSONObject params) throws JsonRpcException, JSONException {
        return doRequest(method, params);
    }

    @Override
    public JSONObject call(@NotNull String method, @NotNull JSONArray params) throws JsonRpcException,JSONException {
        return doRequest(method, params);
    }

    public JSONObject doRequest(@NotNull String method, @NotNull Object params) throws JsonRpcException, JSONException {
        JSONObject jsonRPC = generateJsonRpcObject(method, params);
        JSONObject result = new JSONObject(restUtil.executePost(baseUrl, jsonRPC.toString(), null));
        if (result.has("error")) {
            throw new JsonRpcException(result.getJSONObject("error").has("message") ? result.getJSONObject("error").getString("message") : "Unknown error", result);
        }
        return result.getJSONObject("result");
    }

    private JSONObject generateJsonRpcObject(String method, Object params) {
        try {
            JSONObject jsonRPC = new JSONObject();
            jsonRPC.put("jsonrpc", "2.0");
            jsonRPC.put("method", method);
            jsonRPC.put("params", params);
            jsonRPC.put("id", 1);

            return jsonRPC;
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return null;
    }
}

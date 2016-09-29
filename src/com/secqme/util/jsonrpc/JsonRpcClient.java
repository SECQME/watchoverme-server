package com.secqme.util.jsonrpc;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.validation.constraints.NotNull;

/**
 * Created by edward on 16/07/2015.
 */
public interface JsonRpcClient {
    JSONObject call(@NotNull String method, @NotNull JSONObject params) throws JsonRpcException, JSONException;
    JSONObject call(@NotNull String method, @NotNull JSONArray params) throws JsonRpcException, JSONException;
}

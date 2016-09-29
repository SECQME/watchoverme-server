package com.secqme.util.notification.push;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.push.ContentBasedPushMessageVO;
import com.secqme.domain.model.notification.push.PushMessageRecipientVO;
import com.secqme.util.ar.ARTemplateEngine;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Map;
import java.util.Properties;

/**
 * Created by edward on 22/05/2015.
 */
public class ParsePushService extends BasePushService implements PushService {

    private static final Logger myLog = Logger.getLogger(ParsePushService.class);

    private static final String PUSH_CHANNEL = "channel";
    private static final String PUSH_DATA = "data";
    private static final String PUSH_ALERT = "alert";

    private String parsePushUrl = null;
    private String parsePushApplicationId = null;
    private String parsePushRestApiKey = null;

    public ParsePushService(ARTemplateEngine arTemplateEngine, RestUtil restUtil, String parsePushUrl, String parsePushApplicationId, String parsePushRestApiKey) {
        super(arTemplateEngine, restUtil);
        this.parsePushUrl = parsePushUrl;
        this.parsePushApplicationId = parsePushApplicationId;
        this.parsePushRestApiKey = parsePushRestApiKey;
    }

    @Override
    protected boolean pushContentBasedPushMessageToSingleRecipient(ContentBasedPushMessageVO contentBasedPushMessageVO) {
        try {
            myLog.debug("pushToParse " + parsePushUrl + "*" + parsePushApplicationId + "*" + parsePushRestApiKey);

            JSONObject jsonRequest = createPushRequest(contentBasedPushMessageVO);

            Properties header = new Properties();
            header.put("X-Parse-Application-Id", parsePushApplicationId);
            header.put("X-Parse-REST-API-Key", parsePushRestApiKey);
            String tokenResult = restUtil.executePost(parsePushUrl, jsonRequest.toString(), header);
            myLog.debug("Parse Push Notification result: " + tokenResult);

            // TODO: Save to log

            return true;
        } catch (RestExecException ex) {
            myLog.error("Can't execute REST request", ex);
        } catch (JSONException je) {
            myLog.error("JSON error", je);
        }

        return false;
    }

    private JSONObject createPushRequest(ContentBasedPushMessageVO contentBasedPushMessageVO) throws JSONException {
        PushMessageRecipientVO firstRecipientVO = contentBasedPushMessageVO.getRecipients().get(0);

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put(PUSH_CHANNEL, generateUserChannel(firstRecipientVO.getUserVO()));

        JSONObject jsonData = new JSONObject();
        jsonData.put(PUSH_ALERT, contentBasedPushMessageVO.getBody());
        for (Map.Entry<String, String> entry : contentBasedPushMessageVO.getPayloads().entrySet()) {
            jsonData.put(entry.getKey(), entry.getValue());
        }
        jsonRequest.put(PUSH_DATA, jsonData);

        return jsonRequest;
    }

    private String generateUserChannel(UserVO userVO) {
        return "token_" + userVO.getActivationCode();
    }
}

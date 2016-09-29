package com.secqme.util.marketing;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.util.ar.ParameterizableHashMap;
import com.secqme.util.ar.ParameterizableUtils;
import com.secqme.util.jsonrpc.DefaultJsonRpcClient;
import com.secqme.util.jsonrpc.JsonRpcClient;
import com.secqme.util.jsonrpc.JsonRpcException;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GetResponseMarketingEmailService implements MarketingEmailService {

    private static final Logger myLog = Logger.getLogger(GetResponseMarketingEmailService.class);

    private JsonRpcClient jsonRpcClient;

    private String apiKey;
    private String liteCampaign;
    private String premiumCampaign;
    private String generalCampaign;
    private String emergencyContactFunnel;

    public GetResponseMarketingEmailService(RestUtil restUtil, String targetUrl, String apiKey, String generalCampaign, String liteCampaign,
                                            String premiumCampaign, String emergencyContactFunnel) {
        this.jsonRpcClient = new DefaultJsonRpcClient(restUtil, targetUrl);
        this.apiKey = apiKey;

        this.liteCampaign = liteCampaign;
        this.premiumCampaign = premiumCampaign;
        this.generalCampaign = generalCampaign;
        this.emergencyContactFunnel = emergencyContactFunnel;
    }

    @Override
    public void addUserToCampaign(UserVO userVO, MarketingCampaign campaign) {
        Map<String, Object> customFields = new HashMap<>();
        customFields.put("package", userVO.getPackageVO().getPkgType().toString());

        addEmailToCampaign(userVO.getEmailAddress(), userVO.getNickName(), campaign, customFields);
    }

    @Override
    public void addContactToCampaign(ContactVO contactVO, MarketingCampaign campaign) {
        ParameterizableHashMap<String, Object> customFields = new ParameterizableHashMap<>();
        ParameterizableUtils.fillWithContactPublicData(customFields, contactVO);

        addEmailToCampaign(contactVO.getEmailAddress(), contactVO.getNickName(), campaign, customFields);
    }

    private void addEmailToCampaign(String email, String name, MarketingCampaign campaign, Map<String, Object> customFields) {
        try {
            JSONArray params = addContactJSON(email, name, getCampaignCode(campaign), customFields);
            JSONObject result = jsonRpcClient.call("add_contact", params);
            myLog.debug("Add contact to campaign result: " + result);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (JsonRpcException ex) {
            if ("Contact already added to target campaign".equals(ex.getMessage())) {
                myLog.debug(ex.getMessage(), ex);
            } else {
                myLog.error(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void deleteUserFromCampaign(UserVO userVO, MarketingCampaign campaign) {
        deleteEmailFromCampaign(userVO.getEmailAddress(), campaign);
    }

    @Override
    public void deleteContactFromCampaign(ContactVO contactVO, MarketingCampaign campaign) {
        deleteEmailFromCampaign(contactVO.getEmailAddress(), campaign);
    }

    private void deleteEmailFromCampaign(String email, MarketingCampaign campaign) {
        try {
            JSONArray getContactParams = getContactJSON(email, campaign);
            JSONObject getContactResult = jsonRpcClient.call("get_contacts", getContactParams);
            myLog.debug("Get contact to campaign result: " + getContactResult);

            Iterator keys = getContactResult.keys();
            if (keys.hasNext()) {
                String contactId = (String) keys.next();
                if (contactId != null) {
                    JSONArray deleteParams = deleteContactJSON(contactId);
                    JSONObject deleteResult = jsonRpcClient.call("delete_contact", deleteParams);
                    myLog.debug("Delete contact to campaign result: " + deleteResult);
                }
            }
        } catch (JSONException | JsonRpcException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    private String getCampaignCode(MarketingCampaign campaign) {
        switch (campaign) {
        case GENERAL_CAMPAIGN:
            return generalCampaign;
        case LITE_PACKAGE_CAMPAIGN:
            return liteCampaign;
        case PREMIUM_PACKAGE_CAMPAIGN:
            return premiumCampaign;
        case EMERGENCY_CONTACT_FUNNEL:
            return emergencyContactFunnel;
        }
        throw new IllegalArgumentException("Unknown marketing campaign: " + campaign.toString());
    }

    private JSONArray addContactJSON(String email, String name, String campaignId, Map<String, Object> customFields) {
        try {
            JSONArray jsonArrayParams = generateStandardParams();

            JSONObject jsonObjectParams = new JSONObject();
            jsonObjectParams.put("campaign", campaignId);
            jsonObjectParams.put("name", name);
            jsonObjectParams.put("email", email);
            jsonObjectParams.put("cycle_day", "0");

            if (customFields != null && customFields.size() > 0) {
                JSONArray jsonArrayCustom = new JSONArray();

                for (Map.Entry<String, Object> pair : customFields.entrySet()) {
                    JSONObject jsonObjectCustom = new JSONObject();
                    jsonObjectCustom.put("name", pair.getKey());
                    jsonObjectCustom.put("content", pair.getValue());
                    jsonArrayCustom.put(jsonObjectCustom);
                }

                jsonObjectParams.put("customs", jsonArrayCustom);
            }
            jsonArrayParams.put(jsonObjectParams);
            return jsonArrayParams;
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return null;
    }

    private JSONArray getContactJSON(String email, MarketingCampaign campaign) {
        try {
            JSONArray jsonArrayParams = generateStandardParams();

            JSONArray jsonCampaigns = new JSONArray();
            jsonCampaigns.put(getCampaignCode(campaign));

            JSONObject jsonEmail = new JSONObject();
            jsonEmail.put("EQUALS", email);

            JSONObject jsonSearchCriteria = new JSONObject();
            jsonSearchCriteria.put("email", jsonEmail);
            jsonSearchCriteria.put("campaigns", jsonCampaigns);

            jsonArrayParams.put(jsonSearchCriteria);
            return jsonArrayParams;
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return null;
    }

    private JSONArray deleteContactJSON(String contactId) {
        try {
            JSONArray jsonArrayParams = generateStandardParams();

            JSONObject jsonSearchCriteria = new JSONObject();
            jsonSearchCriteria.put("contact", contactId);

            jsonArrayParams.put(jsonSearchCriteria);
            return jsonArrayParams;
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return null;
    }

    private JSONArray generateStandardParams() {
        JSONArray jsonArrayParams = new JSONArray();
        jsonArrayParams.put(apiKey);
        return jsonArrayParams;
    }
}

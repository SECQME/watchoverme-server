package com.secqme.sns;

import com.secqme.domain.dao.SocialNetworkDAO;
import com.secqme.domain.model.notification.SocialNetworkVO;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class DefaultFacebookUtil implements FacebookUtil {

    public static final String EXPIRES_IN_KEY = "expires_in";
    // https://graph.facebook.com/oauth/access_token?client_id=114494981902191&client_secret=6ba6d2b6481b477742ba8a4e048ae1ad&grant_type=fb_exchange_token&fb_exchange_token=BAABoIfC5j28BAFER3hpdn0QF89sCJ9bGqlTYWiJpdvQuXlawIEbH2JFcPFD89SrhDtLfGmW1OEbcONDUCUimrSkgo3QdYKCtFosxlDiZAdiQJELZBG6H1AHDDIn85yoBZBlL26BvQZDZD
    // access_token=AAABoIfC5j28BAPD9JMHW9D9WRu1PPZAVQ5Y4FbbDCMJhlF3R4m6yj1t7ID2VWAZAK4TJTYRUJfO9fYyXD0jynePiUoZBm4aNsVGmf3HygZDZD&expires=5183872
    private static Logger myLog = Logger.getLogger(DefaultFacebookUtil.class);
    private String apiKey;
    private String apiSecret;
    private String clientID;
    // set this to your servlet URL for the authentication servlet/filter
    private String authURL;
    private SocialNetworkVO fbSNSVO;
    private String fbExchangeTokenURL;
    private RestUtil restUtil;
    /// set this to the list of extended permissions you want

    public DefaultFacebookUtil(SocialNetworkDAO socialNetworkDAO, String fbAppName,
            String oauthURL, String fbExgTokenURL, RestUtil restUtil) {

        authURL = oauthURL;
        fbSNSVO = socialNetworkDAO.read(fbAppName);
        this.restUtil = restUtil;

        try {
            JSONObject fbConfigObj = new JSONObject(fbSNSVO.getSnsConfig());
            apiKey = fbConfigObj.getString("appKey");
            apiSecret = fbConfigObj.getString("appSecret");
            clientID = fbConfigObj.getString("appID");
            fbExchangeTokenURL = fbExgTokenURL.replace("CLIENT_ID", clientID).replace("CLIENT_SECRET", apiSecret);
            myLog.debug("Set up FBConfig for " + fbAppName + ", with Redirect URI set to " + oauthURL + ", exchangeURL->" + fbExchangeTokenURL);
        } catch (JSONException ex) {
            myLog.error("Problem of constructing Facebook Obj", ex);
        }

    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getClientID() {
        return clientID;
    }

    public SocialNetworkVO getFbSNSVO() {
        return fbSNSVO;
    }

    @Override
    public String getLoginRedirectURL() {
        return "https://graph.facebook.com/v2.1/oauth/authorize?client_id="
                + clientID + "&display=page&redirect_uri="
                + authURL + "&scope=" + "publish_stream,offline_access";
    }

    @Override
    public String getAuthURL(String authCode) {
        return "https://graph.facebook.com/v2.3/oauth/access_token?client_id="
                + clientID + "&redirect_uri="
                + authURL + "&client_secret=" + apiSecret + "&code=" + authCode;
    }

    @Override
    public JSONObject renewFBToken(String currentToken) {
        JSONObject jobj = null;
        try {
            Date now = new Date();
            String httpResult = restUtil.executeGet(fbExchangeTokenURL + currentToken, null);
            myLog.debug("Result->" + httpResult);

            jobj = new JSONObject(httpResult);
            jobj.put(FacebookUtil.EXPIRES_KEY, now.getTime() + (jobj.getLong(EXPIRES_IN_KEY) * 1000));
        } catch (JSONException ex) {
            myLog.error("Failed to renew Facebook token: " + currentToken, ex);
        } catch (RestExecException ex) {
            myLog.error("Failed to renew Facebook token: " + currentToken, ex);
        }
        return jobj;
    }
}

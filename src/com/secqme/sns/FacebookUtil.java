package com.secqme.sns;

import com.secqme.domain.model.notification.SocialNetworkVO;
import org.codehaus.jettison.json.JSONObject;


/**
 *
 * @author jameskhoo
 */
public interface FacebookUtil {

    public static final String FB_ACCESS_TOKEN_KEY = "access_token";
    public static final String FACEBOOK_NAME = "facebook";
    public static final String EXPIRES_KEY = "expires";
    
    public String getLoginRedirectURL();
    public String getAuthURL(String authCode);
    public String getApiKey();
    public String getApiSecret();
    public String getClientID();
    public SocialNetworkVO getFbSNSVO();
    public JSONObject renewFBToken(String token);
}

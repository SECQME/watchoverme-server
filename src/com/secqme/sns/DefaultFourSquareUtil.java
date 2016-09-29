package com.secqme.sns;

import com.secqme.domain.model.notification.SocialNetworkVO;

/**
 *
 * @author coolboykl
 */
public class DefaultFourSquareUtil {
    
    private static final String AUTHORIZE__BASE_URL = "https://foursquare.com/oauth2/authorize";
    private static final String ACCESS_TOKEN_BASE_URL = "https://foursquare.com/oauth2/access_token";
    
    private String apiKey;
    private String apiSecret;
    private String apiName;
    private SocialNetworkVO fourSquareSNSVO;
    
    
}

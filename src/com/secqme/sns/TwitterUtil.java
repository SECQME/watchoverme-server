package com.secqme.sns;

import com.secqme.domain.model.notification.SocialNetworkVO;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 *
 * @author jameskhoo
 */
public interface TwitterUtil {
    public final static String TW_SCREEN_NAME_KEY = "screenName";
    public final static String TW_TOKEN_KEY = "token";
    public final static String TW_TOKEN_SECRET_KEY = "tokenSecret";
    public final static String TWITTER_NAME = "twitter";

    public String getAuthURL() throws TwitterException;
    public Twitter getTwitter();
    public SocialNetworkVO getTWSNSVO();
}

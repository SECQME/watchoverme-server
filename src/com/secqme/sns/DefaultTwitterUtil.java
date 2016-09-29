package com.secqme.sns;

import com.secqme.domain.dao.SocialNetworkDAO;
import com.secqme.domain.model.notification.SocialNetworkVO;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.RequestToken;

/**
 *
 * @author jameskhoo
 */
public class DefaultTwitterUtil implements TwitterUtil {

    private String consumerKey;
    private String consumerSecret;
    private Twitter twitter = null;
    private String twitterAppName = null;
    private SocialNetworkVO twitterSocialNetworkVO;
    private static Logger myLog = Logger.getLogger(DefaultTwitterUtil.class);

    public DefaultTwitterUtil(SocialNetworkDAO socialNetworkDAO, String twAppName) {
        myLog.debug("Setting up the Twitter Social Network->" + twAppName);
        this.twitterAppName = twAppName;
        twitterSocialNetworkVO = socialNetworkDAO.read(twAppName);
        try {
            JSONObject jobj = new JSONObject(twitterSocialNetworkVO.getSnsConfig());
            consumerKey = jobj.getString("consumerKey");
            consumerSecret = jobj.getString("consumerSecret");
            twitter = new TwitterFactory().getOAuthAuthorizedInstance(consumerKey, consumerSecret);

        } catch (JSONException ex) {
            myLog.error("JSONException", ex);
        }

    }

    public String getAuthURL() {
        String authURL = null;
        try {
            
            RequestToken requestToken = twitter.getOAuthRequestToken();
            authURL = requestToken.getAuthorizationURL();
            
        } catch (TwitterException ex) {
            myLog.error("Twitter error", ex);
        }
        return authURL;
    }

    public Twitter getTwitter() {
        return new TwitterFactory().getOAuthAuthorizedInstance(consumerKey, consumerSecret);
    }

    public SocialNetworkVO getTWSNSVO() {
        return twitterSocialNetworkVO;
    }

}

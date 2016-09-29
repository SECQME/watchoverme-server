package com.secqme.util.notification.sns;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import com.secqme.sns.TwitterUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;

/**
 *
 * @author jameskhoo
 */
@Deprecated
public class TwitterSNSService implements SNSService {

    private TwitterUtil twitterUtil = null;
    private static Logger myLog = Logger.getLogger(TwitterSNSService.class);

    public TwitterSNSService(TwitterUtil twUtil) {
        twitterUtil = twUtil;
    }

    public String getSNSServiceName() {
        return TwitterUtil.TWITTER_NAME;
    }

    @Override
    public UserSNSConfigVO generateSNSConfig(String providerUserId, JSONObject additionalConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserVO findAllegedlyUser(UserSNSConfigVO userSNSConfigVO) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getEmailAddress(UserSNSConfigVO userSNSConfigVO) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocale(UserSNSConfigVO userSNSConfigVO) {
        return null;
    }

    @Override
    public String getName(UserSNSConfigVO userSNSConfigVO) {
        return null;
    }

    public void publishSNSMessage(UserSNSConfigVO userSNSConfigVO, SNSMessageVO snsMessageVO) throws SNSUpdateException {
        throw new UnsupportedOperationException("Twitter push notification.");

//        myLog.debug("[TW] Updating '" + snsMessageVO + "' for " + userSNSConfigVO.getUserVO().getUserid());
//        Twitter twitter = twitterUtil.getTwitter();
//        try {
//            JSONObject jobj = new JSONObject(userSNSConfigVO.getAdditionalConfig());
//            String token = jobj.getString(TwitterUtil.TW_TOKEN_KEY);
//            String tokenSecret = jobj.getString(TwitterUtil.TW_TOKEN_SECRET_KEY);
//            AccessToken accessToken = new AccessToken(token, tokenSecret);
//            twitter.setOAuthAccessToken(accessToken);
//            twitter.updateStatus(snsMessageVO);
//        } catch (TwitterException ex) {
//            ex.printStackTrace();
//        } catch (JSONException ex) {
//            ex.printStackTrace();
//        }
    }
}

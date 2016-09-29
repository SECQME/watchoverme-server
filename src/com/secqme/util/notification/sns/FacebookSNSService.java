package com.secqme.util.notification.sns;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;
import com.restfb.types.User;
import com.secqme.CoreException;
import com.secqme.domain.dao.SnsLogDAO;
import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.ar.ARMessageTemplateField;
import com.secqme.domain.model.notification.SocialNetworkVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.sns.ContentBasedSNSMessageVO;
import com.secqme.domain.model.notification.sns.SNSLogVO;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import com.secqme.domain.model.notification.sns.TemplateBasedSNSMessageVO;
import com.secqme.sns.FacebookUtil;
import com.secqme.util.ar.ARTemplateEngine;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jameskhoo
 */
public class FacebookSNSService implements SNSService {
    private static final Logger myLog = Logger.getLogger(FacebookSNSService.class);

    private static final Version FB_API_VERSION = Version.VERSION_2_4;
    private static String FB_GRAPH_BY_ACCESS_TOKEN_URL = "https://graph.facebook.com/me/?access_token=";

    private final RestUtil restUtil;
    private final ARTemplateEngine arTemplateEngine;
    private final SocialNetworkVO facebookConfig;
    private final UserDAO userDAO;
    private final SnsLogDAO snsLogDAO;

    private String clientKey;
    private String clientSecret;

    public FacebookSNSService(FacebookUtil facebookUtil, RestUtil restUtil, ARTemplateEngine arTemplateEngine, UserDAO userDAO, SnsLogDAO snsLogDAO) {
        this.facebookConfig = facebookUtil.getFbSNSVO();
        this.restUtil = restUtil;
        this.arTemplateEngine = arTemplateEngine;
        this.userDAO = userDAO;
        this.snsLogDAO = snsLogDAO;

        try {
            JSONObject fbConfigObj = new JSONObject(facebookConfig.getSnsConfig());
            this.clientKey = fbConfigObj.getString("appKey");
            this.clientSecret = fbConfigObj.getString("appSecret");
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    @Override
    public UserSNSConfigVO generateSNSConfig(String providerUserId, JSONObject additionalConfig) {
        try {
            UserSNSConfigVO userSNSConfigVO = new UserSNSConfigVO();
            userSNSConfigVO.setSnsName(getSNSServiceName());
            userSNSConfigVO.setSocialNetworkVO(facebookConfig);
            userSNSConfigVO.setNotify(false);

            FacebookClient client = new DefaultFacebookClient(additionalConfig.getString(FacebookUtil.FB_ACCESS_TOKEN_KEY), FB_API_VERSION);

            User fbUser = client.fetchObject("me", User.class, Parameter.with("fields", "name,email,gender,locale,timezone"));
            userSNSConfigVO.setSnsuid(fbUser.getId());
            additionalConfig.put("name", fbUser.getName());
            additionalConfig.put("email", fbUser.getEmail());
            additionalConfig.put("gender", fbUser.getGender());
            additionalConfig.put("locale", fbUser.getLocale());
            additionalConfig.put("timezone", fbUser.getTimezone());

            FacebookClient.AccessToken extendedAccessToken = client.obtainExtendedAccessToken(clientKey, clientSecret);
            additionalConfig.put(FacebookUtil.FB_ACCESS_TOKEN_KEY, extendedAccessToken.getAccessToken());
            additionalConfig.put(FacebookUtil.EXPIRES_KEY, extendedAccessToken.getExpires().getTime());
            userSNSConfigVO.setAdditionalConfig(additionalConfig);
            userSNSConfigVO.setUpdatedDate(new Date());

            return userSNSConfigVO;
        } catch (JSONException ex) {
            myLog.error("Error parsing JSON object.", ex);
            return null;
        }
    }

    @Override
    public UserVO findAllegedlyUser(UserSNSConfigVO userSNSConfigVO) {
        String emailAddress = getEmailAddress(userSNSConfigVO);
        if (emailAddress != null) {
            return userDAO.findByEmail(emailAddress);
        }
        return null;
    }

    @Override
    public String getEmailAddress(UserSNSConfigVO userSNSConfigVO) {
        return userSNSConfigVO.getAdditionalConfig().optString("email", null);
    }

    @Override
    public String getLocale(UserSNSConfigVO userSNSConfigVO) {
        return userSNSConfigVO.getAdditionalConfig().optString("locale", null);
    }

    @Override
    public String getName(UserSNSConfigVO userSNSConfigVO) {
        return userSNSConfigVO.getAdditionalConfig().optString("name", null);
    }

    @Override
    public void publishSNSMessage(UserSNSConfigVO userSNSConfigVO, SNSMessageVO snsMessageVO) throws SNSUpdateException {
        myLog.debug("[FB] Updating '" + snsMessageVO + "' for " + userSNSConfigVO.getUserVO().getUserid());
        try {
            JSONObject additionalConfig = userSNSConfigVO.getAdditionalConfig();

            FacebookClient client = new DefaultFacebookClient(additionalConfig.getString(FacebookUtil.FB_ACCESS_TOKEN_KEY), FB_API_VERSION);

            // Extend access token
            FacebookClient.AccessToken extendedAccessToken = client.obtainExtendedAccessToken(clientKey, clientSecret);
            additionalConfig.put(FacebookUtil.FB_ACCESS_TOKEN_KEY, extendedAccessToken.getAccessToken());
            additionalConfig.put(FacebookUtil.EXPIRES_KEY, extendedAccessToken.getExpires().getTime());
            userSNSConfigVO.setAdditionalConfig(additionalConfig);
            userSNSConfigVO.setUpdatedDate(new Date());
            this.upsertSNSConfig(userSNSConfigVO.getUserVO(), userSNSConfigVO);

            String message = null;
            if (snsMessageVO instanceof TemplateBasedSNSMessageVO) {
                ContentBasedSNSMessageVO contentBasedSNSMessageVO = ContentBasedSNSMessageVO.fromTemplateBasedSNSMessageVO((TemplateBasedSNSMessageVO) snsMessageVO, arTemplateEngine, ARMessageTemplateField.FACEBOOK_POST);
                message = contentBasedSNSMessageVO.getBody();
            } else if (snsMessageVO instanceof ContentBasedSNSMessageVO) {
                message = ((ContentBasedSNSMessageVO) snsMessageVO).getBody();
            }

            // Publish message
            FacebookType publishMessageResponse =
                    client.publish("me/feed", FacebookType.class,
                            Parameter.with("message", message));

            // Save log
            SNSLogVO snsLogVO = new SNSLogVO();
            snsLogVO.setSnsName(FacebookUtil.FACEBOOK_NAME);
            snsLogVO.setSnsUid(userSNSConfigVO.getSnsuid());
            snsLogVO.setSnsMessageId(publishMessageResponse.getId());
            snsLogVO.setUserid(userSNSConfigVO.getUserid());
            snsLogVO.setMessageType(snsMessageVO.getMessageType());
            snsLogVO.setMessage(message);
            snsLogVO.setCreatedAt(new Date());
            snsLogDAO.create(snsLogVO);
        } catch (JSONException ex) {
            myLog.error("Error parsing JSON object.", ex);
        }
    }

    public String getSNSServiceName() {
        return FacebookUtil.FACEBOOK_NAME;
    }

    public void upsertSNSConfig(UserVO userVO, UserSNSConfigVO newSNSConfigVO) throws CoreException {
        // First Determine if user SNSConfigList is null
        List<UserSNSConfigVO> userSNSConfigList = userVO.getSnsConfigList();
        newSNSConfigVO.setUpdatedDate(new Date());
        myLog.debug("Add or Replacing user SNSConfig for user->" + userVO.getUserid()
                + ", snsName:" + newSNSConfigVO.getSocialNetworkVO().getSnsName()
                + ", notify:" + newSNSConfigVO.isNotify());
        if (userSNSConfigList == null) {
            userSNSConfigList = new ArrayList<UserSNSConfigVO>();
            userSNSConfigList.add(newSNSConfigVO);
            userVO.setSnsConfigList(userSNSConfigList);
        } else {
            // Needs to find out if there is an existing SNSConfig sharing the same SNSVO
            UserSNSConfigVO someSNSConfigVO = null;
            for (UserSNSConfigVO tmpUsrSNSConfigVO : userSNSConfigList) {
                if (tmpUsrSNSConfigVO.getSocialNetworkVO().equals(newSNSConfigVO.getSocialNetworkVO())) {
                    // is a replacment
                    someSNSConfigVO = tmpUsrSNSConfigVO;
                    myLog.debug("Existing SNSConfig Found, notify->" + someSNSConfigVO.isNotify());
                    break;
                }
            }
            if (someSNSConfigVO != null) {
                userVO.getSnsConfigList().remove(someSNSConfigVO);
            }
            userVO.getSnsConfigList().add(newSNSConfigVO);
        }
        userDAO.update(userVO);
    }

    @Deprecated
    public JSONObject getGraphByAccessToken(String snsAccessToken) {
        try {
            String result = restUtil.executeGet(FB_GRAPH_BY_ACCESS_TOKEN_URL + snsAccessToken, null);
            myLog.debug("FB graph response: " + result);
            return new JSONObject(result);
        } catch (RestExecException ex) {
            myLog.error("Failed to REST request.", ex);
        } catch (JSONException ex) {
            myLog.error("Error parsing JSON object.", ex);
        }
        return null;
    }
}

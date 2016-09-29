package com.secqme.util.notification.sns;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by edward on 11/09/2015.
 */
public interface SNSManager {
    public UserSNSConfigVO generateSNSConfig(String providerId, String providerUserId, JSONObject additionalConfig);
    public boolean isRegistered(UserSNSConfigVO userSNSConfigVO);
    public UserVO findAllegedlyUser(UserSNSConfigVO userSNSConfigVO);
    public UserVO prepareNewUserVO(UserSNSConfigVO userSNSConfigVO);
    public void publishNotification(SNSMessageVO snsMessageVO) throws SNSUpdateException;
}

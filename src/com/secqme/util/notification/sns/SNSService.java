
package com.secqme.util.notification.sns;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author jameskhoo
 */
public interface SNSService {
    public String getSNSServiceName();
    public UserSNSConfigVO generateSNSConfig(String providerUserId, JSONObject additionalConfig);
    public UserVO findAllegedlyUser(UserSNSConfigVO userSNSConfigVO);
    public String getEmailAddress(UserSNSConfigVO userSNSConfigVO);
    public String getLocale(UserSNSConfigVO userSNSConfigVO);
    public String getName(UserSNSConfigVO userSNSConfigVO);
    public void publishSNSMessage(UserSNSConfigVO userSNSConfigVO, SNSMessageVO snsMessageVO) throws SNSUpdateException;
}

package com.secqme.util.notification.sns;

import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.dao.UserSNSConfigDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 11/09/2015.
 */
public class DefaultSNSManager implements SNSManager {
    private static final Logger myLog = Logger.getLogger(DefaultSNSManager.class);

    private Map<String, SNSService> snsServices;
    private UserSNSConfigDAO userSNSConfigDAO;
    private UserDAO userDAO;

    public DefaultSNSManager(List<SNSService> snsServices, UserDAO userDAO, UserSNSConfigDAO userSNSConfigDAO) {
        this.snsServices = new HashMap<>();
        for (SNSService snsService : snsServices) {
            this.snsServices.put(snsService.getSNSServiceName().toLowerCase(), snsService);
        }

        this.userDAO = userDAO;
        this.userSNSConfigDAO = userSNSConfigDAO;
    }

    @Override
    public UserSNSConfigVO generateSNSConfig(String providerId, String providerUserId, JSONObject additionalConfig) {
        myLog.debug("Generate new SNS config with provider " + providerId);

        SNSService snsService = findSNSService(providerId);
        return snsService.generateSNSConfig(providerUserId, additionalConfig);
    }

    @Override
    public boolean isRegistered(UserSNSConfigVO userSNSConfigVO) {
        UserSNSConfigVO existingUserSNSConfigVO = userSNSConfigDAO.findBySnsNameAndUid(userSNSConfigVO.getSnsuid(), userSNSConfigVO.getSnsName());
        if (existingUserSNSConfigVO != null) {
            existingUserSNSConfigVO.setAdditionalConfig(userSNSConfigVO.getAdditionalConfig());
            userSNSConfigDAO.update(existingUserSNSConfigVO);
            return true;
        }
        return false;
    }

    @Override
    public UserVO findAllegedlyUser(UserSNSConfigVO userSNSConfigVO) {
        SNSService snsService = findSNSService(userSNSConfigVO.getSnsName());
        return snsService.findAllegedlyUser(userSNSConfigVO);
    }

    @Override
    public UserVO prepareNewUserVO(UserSNSConfigVO userSNSConfigVO) {
        SNSService snsService = findSNSService(userSNSConfigVO.getSnsName());

        UserVO userVO = new UserVO();
        userVO.setNickName(snsService.getName(userSNSConfigVO));
        userVO.setEmailAddress(snsService.getEmailAddress(userSNSConfigVO));
        userVO.setLangCode(snsService.getLocale(userSNSConfigVO));

        return userVO;
    }

    @Override
    public void publishNotification(SNSMessageVO snsMessageVO) throws SNSUpdateException {
        List<UserSNSConfigVO> snsConfigVOs = snsMessageVO.getUserVO().getSnsConfigList();
        for (UserSNSConfigVO snsConfigVO : snsConfigVOs) {
            if (snsConfigVO.isNotify()) {
                try {
                    findSNSService(snsConfigVO.getSnsName()).publishSNSMessage(snsConfigVO, snsMessageVO);
                } catch (Exception ex) {
                    myLog.error(ex.getMessage(), ex);
                }
            }
        }
    }


    private SNSService findSNSService(String providerId) {
        SNSService snsService = snsServices.get(providerId.toLowerCase());
        if (snsService != null) {
            return snsService;
        }
        throw new UnsupportedOperationException("Unknown SNS provider: " + providerId);
    }
}

package com.secqme.manager.notification;

import com.secqme.domain.dao.EmailLogDAO;
import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.notification.email.EmailStatus;
import com.secqme.manager.BaseManager;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by edward on 11/05/2015.
 */
public class DefaultEmailLogManager extends BaseManager implements EmailLogManager {

    @Override
    public EmailLogVO updateLog(String provider, String messageId, EmailStatus status, String city, String state, String country, String failedReason) {
        EmailLogDAO emailLogDAO = getEmailLogDAO();
        EmailLogVO emailLogVO = emailLogDAO.findByProviderAndMessageId(provider, messageId);
        if (emailLogVO != null) {
            if (StringUtils.isNotEmpty(city)) {
                emailLogVO.setCity(city);
            }
            if (StringUtils.isNotEmpty(state)) {
                emailLogVO.setState(state);
            }
            if (StringUtils.isNotEmpty(country)) {
                emailLogVO.setCountry(country);
            }
            if (StringUtils.isNotEmpty(failedReason)) {
                emailLogVO.setFailedReason(failedReason);
            }
            if (!EmailStatus.CLICKED.equals(emailLogVO.getStatus())) {
                emailLogVO.setStatus(status);
            }
            emailLogDAO.update(emailLogVO);
        }
        return emailLogVO;
    }
}

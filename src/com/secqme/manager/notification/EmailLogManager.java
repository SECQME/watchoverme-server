package com.secqme.manager.notification;

import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.notification.email.EmailStatus;

/**
 * Created by edward on 11/05/2015.
 */
public interface EmailLogManager {
    public EmailLogVO updateLog(String provider, String messageId, EmailStatus status, String city, String state, String country, String failedReason);
}

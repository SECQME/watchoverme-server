package com.secqme.domain.dao;

import com.secqme.domain.model.notification.email.EmailLogVO;

/**
 * Created by edward on 07/05/2015.
 */
public interface EmailLogDAO extends BaseDAO<EmailLogVO, Long> {
    public EmailLogVO findByProviderAndMessageId(String provider, String messageId);
}

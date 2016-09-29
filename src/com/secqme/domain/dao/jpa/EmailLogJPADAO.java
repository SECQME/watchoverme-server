package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.EmailLogDAO;
import com.secqme.domain.model.notification.email.EmailLogVO;
import org.apache.log4j.Logger;

/**
 * Created by edward on 08/05/2015.
 */
public class EmailLogJPADAO extends BaseJPADAO<EmailLogVO, Long> implements EmailLogDAO {

    private static Logger myLog = Logger.getLogger(EmailLogJPADAO.class);


    public EmailLogJPADAO() {
        super(EmailLogVO.class);
    }

    @Override
    public EmailLogVO findByProviderAndMessageId(String provider, String messageId) {
        myLog.debug(String.format("EmailLogJPADAO.findByProviderAndMessageId(%s, %s)", provider, messageId));

        JPAParameter parameter  =  new JPAParameter()
                .setParameter("provider", provider)
                .setParameter("messageId", messageId);

        return executeQueryWithSingleResult(EmailLogVO.QUERY_FIND_BY_PROVIDER_AND_MESSAGE_ID, parameter);
    }
}

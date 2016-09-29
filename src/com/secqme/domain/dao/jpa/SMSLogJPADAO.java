package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SMSLogDAO;
import com.secqme.domain.model.notification.sms.SMSLogVO;

import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public class SMSLogJPADAO extends BaseJPADAO<SMSLogVO, Long> implements SMSLogDAO {

    public SMSLogJPADAO() {
        super(SMSLogVO.class);
    }

    public List<SMSLogVO> findUserSMSLogBetweenDates(String userid, Date startTime, Date endTime) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("endTime", endTime).setParameter("startTime", startTime).setParameter("userid", userid);
        return executeQueryWithResultList(SMSLogVO.QUERY_FIND_SMS_LOGS_USER_BETWEEN_DATE, parameter);
    }
}

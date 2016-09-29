package com.secqme.domain.dao;

import com.secqme.domain.model.notification.sms.SMSLogVO;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public interface SMSLogDAO extends BaseDAO<SMSLogVO, Long> {
    public List<SMSLogVO> findUserSMSLogBetweenDates(String userid, Date startTime, Date endTime);
}

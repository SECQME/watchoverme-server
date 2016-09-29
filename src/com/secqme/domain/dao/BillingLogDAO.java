package com.secqme.domain.dao;

import com.secqme.domain.model.billing.BillingLogVO;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public interface BillingLogDAO extends BaseDAO<BillingLogVO, Long> {
    public List<BillingLogVO> findByUserIdWithDateRange(String userid, Date startTime, Date endTime);
    public List<BillingLogVO> findBySecqMeEventId(Long secQMeEventid);
    public List<BillingLogVO> findByBillingCycle(Long billingCycleid);
}

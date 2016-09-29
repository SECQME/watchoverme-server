package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.BillingLogDAO;
import com.secqme.domain.model.billing.BillingLogVO;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public class BillingLogJPADAO extends BaseJPADAO<BillingLogVO, Long> implements BillingLogDAO, Serializable {

    private static Logger myLog = Logger.getLogger(BillingLogJPADAO.class);

    public BillingLogJPADAO() {
        super(BillingLogVO.class);
    }

    public List<BillingLogVO> findByUserIdWithDateRange(String userid, Date startTime, Date endTime) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setParameter("userid", userid);
        return executeQueryWithResultList(BillingLogVO.QUERY_FIND_BY_USER_ID_WITH_DATE_RANGE, parameter);
    }

    public List<BillingLogVO> findBySecqMeEventId(Long secqMeEventid) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("id", secqMeEventid);
        return executeQueryWithResultList(BillingLogVO.QUERY_FIND_BY_SECQME_EVENT_ID, parameter);
    }

    public List<BillingLogVO> findByBillingCycle(Long billingCycleid) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("id", billingCycleid);
        return executeQueryWithResultList(BillingLogVO.QUERY_FIND_BY_BILLING_CYCLE_ID, parameter);
    }
}

package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PaymentHistoryDAO;
import com.secqme.domain.model.payment.PaymentHistoryVO;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public class PaymentHistoryJPADAO extends BaseJPADAO<PaymentHistoryVO, Long> implements PaymentHistoryDAO, Serializable {

    private static Logger myLog = Logger.getLogger(PaymentHistoryJPADAO.class);

    public PaymentHistoryJPADAO() {
        super(PaymentHistoryVO.class);
    }

    public List<PaymentHistoryVO> findByUserIdWithDateRange(String userid, Date startTime, Date endTime) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setParameter("userid", userid);
        return executeQueryWithResultList(PaymentHistoryVO.QUERY_FIND_BY_USER_ID_WITH_DATA_RANGE, parameter);
    }

    public List<PaymentHistoryVO> findByUserId(String userid) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("userid", userid);
        return executeQueryWithResultList(PaymentHistoryVO.QUERY_FIND_BY_USER_ID);
    }

}

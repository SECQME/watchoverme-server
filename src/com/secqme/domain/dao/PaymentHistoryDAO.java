package com.secqme.domain.dao;

import com.secqme.domain.model.payment.PaymentHistoryVO;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public interface PaymentHistoryDAO extends BaseDAO<PaymentHistoryVO, Long> {
    public List<PaymentHistoryVO> findByUserIdWithDateRange(String userid, Date startTime, Date endTime);
    public List<PaymentHistoryVO> findByUserId(String userid);
    
}

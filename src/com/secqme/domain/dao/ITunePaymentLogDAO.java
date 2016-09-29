package com.secqme.domain.dao;

import com.secqme.domain.model.payment.ItunePaymentLogVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface ITunePaymentLogDAO extends BaseDAO<ItunePaymentLogVO, Long> {
    
    public List<ItunePaymentLogVO> findByUserID(String userid);
    public ItunePaymentLogVO findByReceiptData(String receipt);
    
}

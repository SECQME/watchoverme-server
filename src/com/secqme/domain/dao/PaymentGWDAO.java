package com.secqme.domain.dao;

import com.secqme.domain.model.payment.PaymentGWVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface PaymentGWDAO extends BaseDAO<PaymentGWVO, String> {
    public List<PaymentGWVO> findAll();
}

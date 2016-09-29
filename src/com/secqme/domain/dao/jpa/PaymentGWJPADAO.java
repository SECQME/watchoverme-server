package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PaymentGWDAO;
import com.secqme.domain.model.payment.PaymentGWVO;

import java.util.List;

/**
 *
 * @author james
 */
public class PaymentGWJPADAO extends BaseJPADAO<PaymentGWVO, String> implements PaymentGWDAO {

    public PaymentGWJPADAO() {
        super(PaymentGWVO.class);
    }

    public List<PaymentGWVO> findAll() {
        return executeQueryWithResultList(PaymentGWVO.QUERY_FIND_ALL);
    }
}

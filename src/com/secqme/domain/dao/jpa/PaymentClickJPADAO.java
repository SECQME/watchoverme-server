package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PaymentClickDAO;
import com.secqme.domain.model.payment.PaymentClickVO;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class PaymentClickJPADAO  extends BaseJPADAO<PaymentClickVO, Long> implements PaymentClickDAO, Serializable {

    private static Logger myLog = Logger.getLogger(PaymentClickJPADAO.class);

    public PaymentClickJPADAO() {
        super(PaymentClickVO.class);
    }

}

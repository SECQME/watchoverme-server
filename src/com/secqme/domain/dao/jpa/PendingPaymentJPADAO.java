package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PendingPaymentDAO;
import com.secqme.domain.model.payment.PendingPaymentVO;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 *
 * @author james
 */
public class PendingPaymentJPADAO extends BaseJPADAO<PendingPaymentVO, String> implements PendingPaymentDAO, Serializable {

    private static Logger myLog = Logger.getLogger(PendingPaymentJPADAO.class);

    public PendingPaymentJPADAO() {
        super(PendingPaymentVO.class);
    }

}

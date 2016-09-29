package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PayPalIPNDAO;
import com.secqme.domain.model.payment.PayPalIPNLogVO;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 *
 * @author james
 */
public class PayPalIPNJPADAO extends BaseJPADAO<PayPalIPNLogVO, Long> implements PayPalIPNDAO, Serializable {

    private static Logger myLog = Logger.getLogger(PayPalIPNJPADAO.class);

    public PayPalIPNJPADAO() {
        super(PayPalIPNLogVO.class);
    }

}

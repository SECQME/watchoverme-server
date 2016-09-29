package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ITunePaymentLogDAO;
import com.secqme.domain.model.payment.ItunePaymentLogVO;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author james
 */
public class ItunePaymentLogJPADAO extends BaseJPADAO<ItunePaymentLogVO, Long> implements ITunePaymentLogDAO, Serializable {

    private static Logger myLog = Logger.getLogger(ItunePaymentLogJPADAO.class);

    public ItunePaymentLogJPADAO() {
        super(ItunePaymentLogVO.class);
    }

    public List<ItunePaymentLogVO> findByUserID(String userid) {
        JPAParameter parameter = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithResultList(ItunePaymentLogVO.QUERY_FIND_BY_USER_ID, parameter);
    }

    public ItunePaymentLogVO findByReceiptData(String receipt) {
        JPAParameter parameter = new JPAParameter().setParameter("receiptData", receipt);
        return executeQueryWithSingleResult(ItunePaymentLogVO.QUERY_FIND_BY_RECEIPT_DATA, parameter);
    }
}

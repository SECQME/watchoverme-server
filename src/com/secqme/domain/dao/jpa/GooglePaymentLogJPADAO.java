package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.GooglePaymentLogDAO;
import com.secqme.domain.model.payment.GooglePaymentLogVO;

/**
 * User: James Khoo
 * Date: 4/15/14
 * Time: 1:37 PM
 */
public class GooglePaymentLogJPADAO extends BaseJPADAO<GooglePaymentLogVO, Long> implements GooglePaymentLogDAO {

    public GooglePaymentLogJPADAO() {
        super(GooglePaymentLogVO.class);
    }
    
    public GooglePaymentLogVO getUserLatestLogVO(String userid) {
        JPAParameter parameter = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithSingleResult(GooglePaymentLogVO.QUERY_FIND_LATEST_LOG_BY_USER, parameter);
    }
}

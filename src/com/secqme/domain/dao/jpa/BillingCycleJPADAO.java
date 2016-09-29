package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.BillingCycleDAO;
import com.secqme.domain.model.billing.BillingCycleVO;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author james
 */
public class BillingCycleJPADAO extends BaseJPADAO<BillingCycleVO, Long> implements BillingCycleDAO, Serializable {

    public BillingCycleJPADAO() {
        super(BillingCycleVO.class);
    }

    public List<BillingCycleVO> findByUserId(String userid) {
        JPAParameter parameter = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithResultList(BillingCycleVO.QUERY_FIND_ALL_BY_USER, parameter);
    }

    public BillingCycleVO getUserLatestBillingCycleVO(String userid) {
        JPAParameter parameter = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithSingleResult(BillingCycleVO.QUERY_FIND_LATEST_CYCLE_BY_USER, parameter);
    }
}

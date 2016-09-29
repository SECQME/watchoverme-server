package com.secqme.domain.dao;

import com.secqme.domain.model.billing.BillingCycleVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface BillingCycleDAO extends BaseDAO<BillingCycleVO, Long> {
    public List<BillingCycleVO> findByUserId(String userid);
    public BillingCycleVO getUserLatestBillingCycleVO(String userid);
}

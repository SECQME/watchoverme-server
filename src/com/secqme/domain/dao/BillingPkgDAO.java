package com.secqme.domain.dao;

import com.secqme.domain.model.billing.BillingPkgVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface BillingPkgDAO extends BaseDAO<BillingPkgVO, String> {

    public List<BillingPkgVO> findAllBillingPackage();

}

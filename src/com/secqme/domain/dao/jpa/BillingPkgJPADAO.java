package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.BillingPkgDAO;
import com.secqme.domain.model.billing.BillingPkgVO;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author james
 */
public class BillingPkgJPADAO extends BaseJPADAO<BillingPkgVO, String> implements BillingPkgDAO, Serializable {

    private static final Logger myLog = Logger.getLogger(BillingPkgJPADAO.class);

    public BillingPkgJPADAO() {
        super(BillingPkgVO.class);
    }

    public List<BillingPkgVO> findAllBillingPackage() {
        return executeQueryWithResultList(BillingPkgVO.QUERY_FIND_ALL);
    }
}

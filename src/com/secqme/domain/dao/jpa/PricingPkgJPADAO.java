package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PricingPackageDAO;
import com.secqme.domain.model.payment.PricingPackageVO;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public class PricingPkgJPADAO extends BaseJPADAO<PricingPackageVO, String> implements PricingPackageDAO, Serializable {

    private static final Logger myLog = Logger.getLogger(PricingPkgJPADAO.class);

    public PricingPkgJPADAO() {
        super(PricingPackageVO.class);
    }



    public List<PricingPackageVO> findAllActivePackageWithOutPromotion(String marketCode) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("marketCode", marketCode)
                .setParameter("todayDate", new Date())
                .setParameter("active", Boolean.TRUE);
        return executeQueryWithResultList(PricingPackageVO.QUERY_FIND_ACTIVE_PACKAGE_WITHOUT_PROMOTION_BY_MARKET, parameter);
    }

    public List<PricingPackageVO> findAllActivePackageWithPromotion(String marketCode) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("todayDate", new Date())
                .setParameter("marketCode", marketCode)
                .setParameter("active", Boolean.TRUE);
        return executeQueryWithResultList(PricingPackageVO.QUERY_FIND_ALL_ACTIVE_PACKAGE_WITHPROMTOTION, parameter);
    }
}

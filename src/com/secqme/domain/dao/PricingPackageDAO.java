package com.secqme.domain.dao;

import com.secqme.domain.model.payment.PricingPackageVO;

import java.util.List;

/**
 *
 * @author james
 */
public interface PricingPackageDAO extends BaseDAO<PricingPackageVO, String> {

    public List<PricingPackageVO> findAllActivePackageWithOutPromotion(String marketCode);
    public List<PricingPackageVO> findAllActivePackageWithPromotion(String marketCode);

}

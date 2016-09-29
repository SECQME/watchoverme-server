package com.secqme.domain.dao;

import com.secqme.domain.model.promocode.PromoCodeVO;

/**
 *
 * @author james
 */
public interface PromoCodeDAO extends BaseDAO<PromoCodeVO, String> {
    public PromoCodeVO findActivePromoCode(String code);
}

package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PromoCodeDAO;
import com.secqme.domain.model.ar.ARMarketMessageTemplateVO;
import com.secqme.domain.model.promocode.PromoCodeVO;

/**
 * Created by edward on 28/07/2015.
 */
public class PromoCodeJPADAO extends BaseJPADAO<PromoCodeVO, String> implements PromoCodeDAO {

    public PromoCodeJPADAO() {
        super(PromoCodeVO.class);
    }

    @Override
    public PromoCodeVO findActivePromoCode(String code) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("code", code);
        return executeQueryWithSingleResult(PromoCodeVO.FIND_ACTIVE_PROMO_CODE, parameter);
    }
}

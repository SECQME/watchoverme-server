package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PromoCodeLogDAO;
import com.secqme.domain.model.promocode.PromoCodeLogVO;
import com.secqme.domain.model.promocode.PromoCodeVO;

import java.util.List;

/**
 * Created by edward on 28/07/2015.
 */
public class PromoCodeLogJPADAO extends BaseJPADAO<PromoCodeLogVO, Long> implements PromoCodeLogDAO {

    public PromoCodeLogJPADAO() {
        super(PromoCodeLogVO.class);
    }

    @Override
    public List<PromoCodeLogVO> findByPromoCodeAndUser(String code, String userid) {
        JPAParameter parameters = new JPAParameter()
                .setParameter("promoCode", code)
                .setParameter("userid", userid);
        return executeQueryWithResultList(PromoCodeLogVO.FIND_BY_CODE_AND_USER, parameters);
    }

    public boolean hasAppliedPromoCode(String userid) {
        JPAParameter parameters = new JPAParameter()
                .setParameter("userid", userid);
        PromoCodeLogVO promoCodeLogVO = executeQueryWithSingleResult(PromoCodeLogVO.FIND_USER_PROMO_CODE, parameters);
        return promoCodeLogVO != null;
    }
}

package com.secqme.domain.dao;

import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.promocode.PromoCodeLogVO;

import java.util.List;

/**
 * Created by edward on 07/05/2015.
 */
public interface PromoCodeLogDAO extends BaseDAO<PromoCodeLogVO, Long> {
    public List<PromoCodeLogVO> findByPromoCodeAndUser(String code, String userid);
    public boolean hasAppliedPromoCode(String userid);
}

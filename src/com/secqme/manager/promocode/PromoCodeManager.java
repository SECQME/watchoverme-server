package com.secqme.manager.promocode;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.promocode.PromoCodeLogVO;
import com.secqme.domain.model.promocode.PromoCodeVO;
import com.secqme.manager.BaseManager;

/**
 * Created by edward on 28/07/2015.
 */
public interface PromoCodeManager {
    public PromoCodeVO validatePromoCode(UserVO userVO, String code);
    public PromoCodeVO applyPromoCode(UserVO userVO, String code);
}

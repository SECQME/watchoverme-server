package com.secqme.manager.promotion;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.promotion.PromotionVO;

/**
 * User: James Khoo
 * Date: 12/9/13
 * Time: 4:01 PM
 */
public interface PromotionManager {
    public PromotionVO getUserCurrentPromotion(UserVO userVO) throws CoreException;
    public BillingPkgVO getFreemiumBillingPkg(String promoteCode);
}

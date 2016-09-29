package com.secqme.domain.dao;

import com.secqme.domain.model.referral.ReferralProgramMarketingTextVO;
import com.secqme.domain.model.referral.ReferralProgramMarketingTextVOPK;

import java.util.List;

/**
 * User: James Khoo
 * Date: 3/4/14
 * Time: 4:40 PM
 */
public interface ReferralProgramMarketingTextDAO extends BaseDAO<ReferralProgramMarketingTextVO, ReferralProgramMarketingTextVOPK> {
    public List<ReferralProgramMarketingTextVO> findAll();
}

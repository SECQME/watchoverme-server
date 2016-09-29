package com.secqme.domain.dao;

import com.secqme.domain.model.referral.ReferralClickType;
import com.secqme.domain.model.referral.ReferralLogVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 3/15/14
 * Time: 11:00 PM
 */
public interface ReferralLogDAO extends BaseDAO<ReferralLogVO, Long> {
    public List<ReferralLogVO> findReferralLogAvailableHTMLParameters(ReferralClickType clickType,
                                                               String requestIP,
                                                               Integer interval);

    public List<ReferralLogVO> findReferralLogByOrgRefUserID(String userid);


}

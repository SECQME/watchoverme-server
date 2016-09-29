package com.secqme.domain.dao;

import com.secqme.domain.model.referral.ReferralReportVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 3/19/14
 * Time: 3:43 PM
 */
public interface ReferralReportDAO extends BaseDAO<ReferralReportVO, Long> {
    public List<ReferralReportVO> findByOriginalReferralUserId(String userid);
}

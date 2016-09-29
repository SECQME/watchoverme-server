package com.secqme.domain.dao;

import com.secqme.domain.model.referral.ReferralProgramVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 2/17/14
 * Time: 6:43 PM
 */
public interface ReferralProgramDAO extends BaseDAO<ReferralProgramVO, String> {
    public List<ReferralProgramVO> findAll();
}

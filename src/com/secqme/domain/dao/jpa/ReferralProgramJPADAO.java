package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ReferralProgramDAO;
import com.secqme.domain.model.referral.ReferralProgramVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 2/17/14
 * Time: 6:44 PM
 */
public class ReferralProgramJPADAO extends BaseJPADAO<ReferralProgramVO, String> implements ReferralProgramDAO {
    public ReferralProgramJPADAO() {
        super(ReferralProgramVO.class);
    }

    public List<ReferralProgramVO> findAll() {
        return executeQueryWithResultList(ReferralProgramVO.QUERY_FIND_ALL);
    }


}

package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ReferralProgramMarketingTextDAO;
import com.secqme.domain.model.referral.ReferralProgramMarketingTextVO;
import com.secqme.domain.model.referral.ReferralProgramMarketingTextVOPK;

import java.util.List;

/**
 * User: James Khoo
 * Date: 3/4/14
 * Time: 4:43 PM
 */
public class ReferralProgramMarketingTextJPADAO extends BaseJPADAO<ReferralProgramMarketingTextVO, ReferralProgramMarketingTextVOPK>
        implements ReferralProgramMarketingTextDAO {
    public ReferralProgramMarketingTextJPADAO() {
        super(ReferralProgramMarketingTextVO.class);
    }

    public List<ReferralProgramMarketingTextVO> findAll() {
        return this.executeQueryWithResultList(ReferralProgramMarketingTextVO.QUERY_FIND_ALL);
    }
}

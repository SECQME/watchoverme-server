package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ReferralReportDAO;
import com.secqme.domain.model.referral.ReferralReportVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 3/19/14
 * Time: 3:44 PM
 */
public class ReferralReportJPADAO extends BaseJPADAO<ReferralReportVO, Long> implements ReferralReportDAO {
    public ReferralReportJPADAO() {
        super(ReferralReportVO.class);
    }

    public List<ReferralReportVO> findByOriginalReferralUserId(String userid) {
        JPAParameter parameter = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithResultList(ReferralReportVO.QUERY_FIND_BY_ORG_USER_ID, parameter);
    }
}

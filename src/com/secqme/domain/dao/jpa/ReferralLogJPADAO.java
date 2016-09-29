package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ReferralLogDAO;
import com.secqme.domain.model.referral.ReferralClickType;
import com.secqme.domain.model.referral.ReferralLogVO;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;
import java.util.List;

/**
 * User: James Khoo
 * Date: 3/15/14
 * Time: 11:06 PM
 */
public class ReferralLogJPADAO extends BaseJPADAO<ReferralLogVO, Long> implements ReferralLogDAO {

    public ReferralLogJPADAO() {
        super(ReferralLogVO.class);
    }


    public List<ReferralLogVO> findReferralLogAvailableHTMLParameters(ReferralClickType clickType,
                                                                      String requestIP,
                                                                      Integer intervalInMinutes) {
        Date endTime = new Date();
        Date startTime = DateUtils.addMinutes(endTime, -intervalInMinutes);
        myLog.debug("Finding referralLog startTime->" + startTime + "," + endTime + ", clickType->" + clickType);

        JPAParameter parameters = new JPAParameter().setParameter("clickType", clickType)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setParameter("requestIP", requestIP);

        return executeQueryWithResultList(ReferralLogVO.QUERY_FIND_BY_HTML_PARAMETERS, parameters);
    }


    public List<ReferralLogVO> findReferralLogByOrgRefUserID(String userid) {
        JPAParameter parameters = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithResultList(ReferralLogVO.QUERY_FIND_BY_REF_USER_ID, parameters);
    }
}

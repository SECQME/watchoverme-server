package com.secqme.domain.model.referral;

import com.secqme.domain.model.UserVO;
import com.secqme.rs.v2.CommonJSONKey;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * User: James Khoo
 * Date: 3/19/14
 * Time: 6:11 PM
 */
public class ReferralInfoVO implements Serializable {
    private static final Logger myLog = Logger.getLogger(ReferralInfoVO.class);

    private List<ReferralLogVO> referralLogVOList;
    private List<ReferralReportVO> referralReportVOList;
    private UserVO userVO;
    private ReferralProgramVO referralProgramVO;
    private Integer totalClick;
    private Integer totalInstall;
    private Integer totalRegister;

    public ReferralInfoVO(UserVO userVO,
                          List<ReferralLogVO> referralLogVOList, List<ReferralReportVO> referralReportVOList,
                          ReferralProgramVO referralProgramVO) {
        this.referralLogVOList = referralLogVOList;
        this.referralReportVOList = referralReportVOList;
        this.userVO = userVO;
        this.referralProgramVO = referralProgramVO;
        updateReferralCount();
    }

    public List<ReferralLogVO> getReferralLogVOList() {
        return referralLogVOList;
    }

    public void setReferralLogVOList(List<ReferralLogVO> referralLogVOList) {
        this.referralLogVOList = referralLogVOList;
        updateReferralCount();
    }

    public List<ReferralReportVO> getReferralReportVOList() {
        return referralReportVOList;
    }

    public void setReferralReportVOList(List<ReferralReportVO> referralReportVOList) {
        this.referralReportVOList = referralReportVOList;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    public ReferralProgramVO getReferralProgramVO() {
        return referralProgramVO;
    }

    public void setReferralProgramVO(ReferralProgramVO referralProgramVO) {
        this.referralProgramVO = referralProgramVO;
    }

    private void updateReferralCount() {
        totalClick = 0;
        totalInstall = 0;
        totalRegister = 0;
        if(referralLogVOList != null && referralLogVOList.size() > 0) {
            for(ReferralLogVO refLogVO : referralLogVOList) {
                switch(refLogVO.getClickType()) {
                    case CLICK:
                        totalClick++;
                        break;
                    case INSTALL:
                        totalInstall++;
                        break;
                    case REGISTER:
                        totalRegister++;
                        break;
                }

            }

        }
    }

    public JSONObject toJSONObject() {
        JSONObject refObj = new JSONObject();
        try {
            refObj.put(CommonJSONKey.USER_REFERRAL_URL_KEY, userVO.getReferralShortURL())
                  .put(CommonJSONKey.REFERRAL_CLICK_COUNT_KEY, totalClick)
                  .put(CommonJSONKey.REFERRAL_INSTALL_COUNT_KEY, totalInstall)
                  .put(CommonJSONKey.REFERRAL_REGISTER_COUNT_KEY, totalRegister);

            if(referralProgramVO != null) {
                refObj.put(CommonJSONKey.REFERRAL_MARKETING_TEXT_KEY, referralProgramVO.getMarketingText());
            }
            JSONArray referralRewardArray = new JSONArray();
            if(referralReportVOList != null && referralReportVOList.size() > 0) {
                for(ReferralReportVO reportVO : referralReportVOList) {
                    JSONObject reportObj = new JSONObject();
                    reportObj.put(CommonJSONKey.REFERRAL_FRIEND_NAME_KEY, reportVO.getRefUserVO().getNickName())
                            .put(CommonJSONKey.REFERRAL_AWARDED_DURATION_KEY, reportVO.getOrgBillCycleDaysRewarded());
                    referralRewardArray.put(reportObj);
                }

            }
            refObj.put(CommonJSONKey.REFERRAL_LIST_KEY, referralRewardArray);
        } catch(JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }
        return refObj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferralInfoVO that = (ReferralInfoVO) o;

        if (referralLogVOList != null ? !referralLogVOList.equals(that.referralLogVOList) : that.referralLogVOList != null)
            return false;
        if (referralProgramVO != null ? !referralProgramVO.equals(that.referralProgramVO) : that.referralProgramVO != null)
            return false;
        if (referralReportVOList != null ? !referralReportVOList.equals(that.referralReportVOList) : that.referralReportVOList != null)
            return false;
        if (userVO != null ? !userVO.equals(that.userVO) : that.userVO != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = referralLogVOList != null ? referralLogVOList.hashCode() : 0;
        result = 31 * result + (referralReportVOList != null ? referralReportVOList.hashCode() : 0);
        result = 31 * result + (userVO != null ? userVO.hashCode() : 0);
        result = 31 * result + (referralProgramVO != null ? referralProgramVO.hashCode() : 0);
        return result;
    }
}

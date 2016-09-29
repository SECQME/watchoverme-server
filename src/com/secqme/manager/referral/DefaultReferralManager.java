package com.secqme.manager.referral;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.referral.*;
import com.secqme.manager.BaseManager;
import com.secqme.manager.billing.BillingManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * User: James Khoo
 * Date: 3/4/14
 * Time: 4:46 PM
 */
public class DefaultReferralManager extends BaseManager implements ReferralManager {

    private static final Logger myLog = Logger.getLogger(DefaultReferralManager.class);
    private HashMap<String, ReferralProgramMarketingTextVO> referralProgramMarketingTextVOHashMap = null;
    private List<ReferralProgramVO> referralProgramVOList = null;
    private String referralBaseURL = null;
    private BillingManager billingManager;

    public DefaultReferralManager(String referralBaseURL, BillingManager billMgr) {
        this.referralBaseURL = referralBaseURL;
        this.billingManager = billMgr;
    }

    public void initReferralProgram() {
        referralProgramMarketingTextVOHashMap = new HashMap<String, ReferralProgramMarketingTextVO>();
        referralProgramVOList = getReferralProgramDAO().findAll();
        List<ReferralProgramMarketingTextVO> referralProgramMarketingTextVOList = getReferralProgramMarketingTextDAO().findAll();
        if (referralProgramMarketingTextVOList != null) {
            referralProgramMarketingTextVOHashMap = new HashMap<String, ReferralProgramMarketingTextVO>();
            for (ReferralProgramMarketingTextVO referralProgramMarketingTextVO : referralProgramMarketingTextVOList) {
                referralProgramMarketingTextVOHashMap.put(referralProgramMarketingTextVO.getCode() + "_" +
                        referralProgramMarketingTextVO.getLangCode(), referralProgramMarketingTextVO);
            }
        }

    }

    public String getReferralURL(UserVO userVO) {
        return this.referralBaseURL + userVO.getActivationCode();
    }

    public ReferralProgramVO getReferralProgramByUser(UserVO userVO) {
        if (referralProgramVOList == null) {
            initReferralProgram();
        }
        ReferralProgramVO userRefProgram = null;
        Date todayDate = new Date();
        for (ReferralProgramVO referralProgram : referralProgramVOList) {
            if (referralProgram.getOrgBillPkg().getPkgName().equalsIgnoreCase(userVO.getPackageVO().getPkgName()) &&
                    todayDate.getTime() >= referralProgram.getStartDate().getTime() &&
                    referralProgram.getVersion().doubleValue() == userVO.getClientVersion()) {
                if (referralProgram.getEndDate() == null ||
                        referralProgram.getEndDate().getTime() >= todayDate.getTime()) {
                    userRefProgram = referralProgram;
                }
            }
        }

        if (userRefProgram != null) {
            ReferralProgramMarketingTextVO referralProgramMarketingTextVO =
                    referralProgramMarketingTextVOHashMap.get(userRefProgram.getMarketingTextCode() + "_" +
                            userVO.getLangCode());
            if (referralProgramMarketingTextVO != null) {
                userRefProgram.setMarketingText(referralProgramMarketingTextVO.getMarketingText());
            }
        }

        return userRefProgram;
    }

    public ReferralLogVO insertReferralLogs(String refURL, ReferralClickType referralClickType,
                                            String requestIP, UserVO refUserVO,
                                            String userAgent, String acceptLanguage,
                                            String sessionID, String fingerPrint) throws CoreException {
        ReferralLogVO referralLogVO = new ReferralLogVO();
        referralLogVO.setReferralURL(refURL);
        referralLogVO.setClickType(referralClickType);
        referralLogVO.setClickTime(new Date());
        referralLogVO.setRequestIP(requestIP);
        referralLogVO.setRefUserVO(refUserVO);
        referralLogVO.setUserAgent(userAgent);
        referralLogVO.setAcceptLanguage(acceptLanguage);
        referralLogVO.setSessionID(sessionID);
        referralLogVO.setFingerPrint(fingerPrint);
        getReferralLogDAO().create(referralLogVO);

        return referralLogVO;

    }

    public ReferralLogVO findReferralLogMatchWithFingerPrint(ReferralClickType referralClickType,
                                                             String requestIP, String fingerPrint,
                                                             Integer intervalInMinutes) {

        ReferralLogVO theReferralLogVO = null;
        myLog.debug("Attempt to find with mapping fingerPrint->" + fingerPrint);
        List<ReferralLogVO> referralLogVOList =
                getReferralLogDAO().findReferralLogAvailableHTMLParameters(referralClickType, requestIP, intervalInMinutes);
        int maxLavDifferent = 50000;

        if (referralLogVOList != null && referralLogVOList.size() > 0) {
            for (ReferralLogVO refLogVO : referralLogVOList) {
                int lavResult = StringUtils.getLevenshteinDistance(fingerPrint, refLogVO.getFingerPrint());
                myLog.debug(refLogVO.getId() + ", time->" + refLogVO.getClickTime() + ", Lav->" + lavResult);
                if (lavResult < maxLavDifferent) {
                    theReferralLogVO = refLogVO;
                    maxLavDifferent = lavResult;
                }
            }
        }

        return theReferralLogVO;
    }

    public void rewardUser(UserVO orgUserVO, UserVO refUserVO) throws CoreException {
        // First Create the Referral Report
        ReferralProgramVO referralProgramVO = getReferralProgramByUser(orgUserVO);
        ReferralReportVO referralReportVO = new ReferralReportVO();
        referralReportVO.setOrgUserVO(orgUserVO);
        if(referralProgramVO.getOrgUpgradeBillPkg() != null) {
        	referralReportVO.setOrgBillPkgRewarded(referralProgramVO.getOrgUpgradeBillPkg().getPkgName());
        }
        referralReportVO.setOrgBillCycleDaysRewarded(referralProgramVO.getOrgBillCycleDaysExtension());
        referralReportVO.setOrgSMSRewarded(referralProgramVO.getOrgSMSAddition());
        referralReportVO.setRefUserVO(refUserVO);
        if(referralProgramVO.getReferrerBillPkg() != null) {
        	referralReportVO.setRefBillPkgRewarded(referralProgramVO.getReferrerBillPkg().getPkgName());
        }
        referralReportVO.setRefBillCycleDaysRewarded(referralProgramVO.getReferrerBillCycleDays());
        referralReportVO.setRefSMSRewarded(referralProgramVO.getReferrerSMSAddition());
        referralReportVO.setTransactionDate(new Date());

        getReferralReportDAO().create(referralReportVO);
        myLog.debug("Created ReferralReport for:" + orgUserVO.getUserid() +
                ", referralProgramCode->" + referralProgramVO.getReferralCode() +
                ", referral UserName->" + refUserVO.getNickName());

        billingManager.executeReferralRewards(orgUserVO, refUserVO, referralProgramVO);
    }

    public ReferralInfoVO getReferralInfoVO(UserVO userVO) {
        ReferralProgramVO referralProgramVO = getReferralProgramByUser(userVO);
        List<ReferralLogVO> referralLogVOList = getReferralLogDAO().findReferralLogByOrgRefUserID(userVO.getUserid());
        List<ReferralReportVO> referralReportVOList = getReferralReportDAO().findByOriginalReferralUserId(userVO.getUserid());

        return new ReferralInfoVO(userVO, referralLogVOList, referralReportVOList, referralProgramVO);
    }

}


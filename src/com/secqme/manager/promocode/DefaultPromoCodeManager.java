package com.secqme.manager.promocode;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.dao.PromoCodeDAO;
import com.secqme.domain.dao.PromoCodeLogDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.billing.BillingLogType;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.promocode.PromoCodeLogVO;
import com.secqme.domain.model.promocode.PromoCodeRewardVO;
import com.secqme.domain.model.promocode.PromoCodeVO;
import com.secqme.manager.UserManager;
import com.secqme.manager.billing.BillingManager;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by edward on 28/07/2015.
 */
public class DefaultPromoCodeManager implements PromoCodeManager {
    private static final Logger myLog = Logger.getLogger(DefaultPromoCodeManager.class);

    public static final String START_AT = "startAt";
    public static final String END_AT = "endAt";
    public static final String MAX_PER_USER = "maxPerUser";
    public static final String SIGN_UP_TIME_RANGE = "signUpTimeRange";

    private PromoCodeDAO promoCodeDAO;
    private PromoCodeLogDAO promoCodeLogDAO;

    private BillingManager billingManager;

    public DefaultPromoCodeManager(PromoCodeDAO promoCodeDAO, PromoCodeLogDAO promoCodeLogDAO, BillingManager billingManager) {
        this.promoCodeDAO = promoCodeDAO;
        this.promoCodeLogDAO = promoCodeLogDAO;
        this.billingManager = billingManager;
    }

    @Override
    public PromoCodeVO validatePromoCode(UserVO userVO, String code) throws CoreException {
        code = code.toUpperCase();
        PromoCodeVO promoCodeVO = promoCodeDAO.findActivePromoCode(code);
        if (promoCodeVO != null) {
            boolean result = false;
            switch (promoCodeVO.getType()) {
                case SIGN_UP:
                    result = isValidSignUpPromoCode(userVO, promoCodeVO);
                    break;
                case TIME_BASED:
                    result = isValidTimeBasedPromoCode(userVO, promoCodeVO);
                    break;
            }

            myLog.debug("Validate promo code " + code + " : " + result);
            if (result) {
                promoCodeVO.setUserReward(findPromoCodeReward(userVO, promoCodeVO));
                if (promoCodeVO.getUserReward() != null) {
                    return promoCodeVO;
                }
            }
        }

        myLog.debug("Invalid promo code: " + code);
        String langCode = (userVO != null) ? userVO.getLangCode() : UserManager.USER_DEFAULT_LANGUAGE;
        throw new CoreException(ErrorType.PROMO_CODE_INVALID, langCode);
    }

    @Override
    public PromoCodeVO applyPromoCode(UserVO userVO, String code) throws CoreException {
        code = code.toUpperCase();
        PromoCodeVO promoCodeVO = validatePromoCode(userVO, code);
        PromoCodeRewardVO promoCodeRewardVO = promoCodeVO.getUserReward();

        switch (promoCodeVO.getType()) {
            case REFERRAL:
                throw new UnsupportedOperationException("Referral code is not implemented yet.");
            default:
                billingManager.rewardUser(userVO, promoCodeRewardVO.getRewardBillingPackageVO(), promoCodeRewardVO.getRewardBillingCycleDays(), promoCodeRewardVO.getRewardSMS(), BillingLogType.PROMO_CODE);
                break;
        }
        savePromoCodeLog(userVO, promoCodeVO, promoCodeRewardVO);

        return promoCodeVO;
    }

    private PromoCodeLogVO savePromoCodeLog(UserVO userVO, PromoCodeVO promoCodeVO, PromoCodeRewardVO promoCodeRewardVO) {
        PromoCodeLogVO logVO = new PromoCodeLogVO();
        logVO.setUserid(userVO.getUserid());
        logVO.setPromoCode(promoCodeVO.getCode());
        logVO.setRewardName(promoCodeRewardVO.getRewardName());
        logVO.setForBillingPackage(promoCodeRewardVO.getForBillingPackageVO() == null ? null : promoCodeRewardVO.getForBillingPackageVO().getPkgName());
        logVO.setRewardBillingPackage(promoCodeRewardVO.getRewardBillingPackageVO() == null ? null : promoCodeRewardVO.getRewardBillingPackageVO().getPkgName());
        logVO.setRewardBillingCycleDays(promoCodeRewardVO.getRewardBillingCycleDays());
        logVO.setRewardSMS(promoCodeRewardVO.getRewardSMS());
        logVO.setCreatedAt(new Date());
        promoCodeLogDAO.create(logVO);

        return logVO;
    }

    private boolean isValidTimeBasedPromoCode(UserVO userVO, PromoCodeVO promoCodeVO) {
        JSONObject promoConfig = promoCodeVO.getConfig();

        if (userVO != null) {
            try {
                long startTime = promoConfig.getLong(START_AT);
                long endTime = promoConfig.getLong(END_AT);
                int maxPerUser = promoConfig.getInt(MAX_PER_USER);

                long now = new Date().getTime();

                if (startTime <= now && now <= endTime) {
                    if (maxPerUser < 0) {
                        return isValidReward(userVO, promoCodeVO);
                    } else {
                        List<PromoCodeLogVO> logs = promoCodeLogDAO.findByPromoCodeAndUser(promoCodeVO.getCode(), userVO.getUserid());
                        int count = (logs != null) ? logs.size() : 0;

                        if (count >= maxPerUser) {
                            myLog.debug(String.format("User %s has applied promo code %s for %d times (Limit is %d).", userVO.getUserid(), promoCodeVO.getCode(), count, maxPerUser));
                            return false;
                        }
                        return true;
                    }
                }
            } catch (JSONException ex) {
                myLog.error(ex.getMessage());
            }
        }

        myLog.debug("Unknown invalid time based promo code");
        return false;
    }

    private boolean isValidSignUpPromoCode(UserVO userVO, PromoCodeVO promoCodeVO) {
        JSONObject promoConfig = promoCodeVO.getConfig();

        try {
            long startTime = promoConfig.getLong(START_AT);
            long endTime = promoConfig.getLong(END_AT);
            long signUpTimeRange = promoConfig.getLong(SIGN_UP_TIME_RANGE);

            long now = new Date().getTime();

            if (startTime <= now && now <= endTime) {
                if (userVO != null) {
                    if (promoCodeLogDAO.hasAppliedPromoCode(userVO.getUserid())) {
                        myLog.debug(String.format("User %s has applied other promo codes.", userVO.getUserid()));
                        return false;
                    } else {
                        long timeSinceSignUp = now - userVO.getCreatedDate().getTime();
                        if (timeSinceSignUp > signUpTimeRange){
                            myLog.debug(String.format("User %s has registered since %d ago (Limit: %d).", userVO.getUserid(), timeSinceSignUp, signUpTimeRange));
                            return false;
                        }
                        return true;
                    }
                } else {
                    return true;
                }
            }
        } catch (JSONException ex) {
            myLog.error(ex.getMessage());
        }

        myLog.debug("Unknown invalid signup promo code");
        return false;
    }

    private boolean isValidReward(UserVO userVO, PromoCodeVO promoCodeVO) {
        return findPromoCodeReward(userVO, promoCodeVO) != null;
    }

    private PromoCodeRewardVO findPromoCodeReward(UserVO userVO, PromoCodeVO promoCodeVO) {
        List<PromoCodeRewardVO> rewards = promoCodeVO.getRewards();

        for (PromoCodeRewardVO reward : rewards) {
            BillingPkgVO forBillingPackage = reward.getForBillingPackageVO();
            BillingPkgVO rewardBillingPackageVO = reward.getRewardBillingPackageVO();

            if (forBillingPackage != null && !forBillingPackage.getPkgName().equalsIgnoreCase(userVO.getPackageVO().getPkgName())) {
                continue;
            }

            if (userVO.getPackageVO().getPkgName().equalsIgnoreCase(rewardBillingPackageVO.getPkgName())) {
                return reward;
            } else {
                // TODO: if ref user is alerady premium, do nothing for now
                if (BillingPkgType.PREMIUM == userVO.getPackageVO().getPkgType()) {
                    continue;
                } else {
                    return reward;
                }
            }
        }

        myLog.debug(String. format("No valid reward found for %s (billing package: %s) in promo code %s.", userVO.getId(), userVO.getPackageVO().getPkgName(), promoCodeVO.getCode()));
        return null;
    }
}

package com.secqme.manager.billing;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.dao.BillingCycleDAO;
import com.secqme.domain.dao.BillingLogDAO;
import com.secqme.domain.dao.BillingPkgDAO;
import com.secqme.domain.dao.MarketDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.*;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.promotion.PromotionVO;
import com.secqme.domain.model.referral.ReferralProgramVO;
import com.secqme.manager.BaseManager;
import com.secqme.util.MarketingTextType;
import com.secqme.util.cache.CacheUtil;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author james
 */
public class DefaultBillingManager extends BaseManager implements BillingManager, Serializable {

    private static final Logger myLog = Logger.getLogger(DefaultBillingManager.class);
    private static final String BILL_LOG_PROVISION_REMARK_KEY = "billingLogProvisionRemark";
    private static final String DEFAULT_MARKET_NAME = "default";
    private final BillingPkgDAO billingPkgDAO;
    private final BillingCycleDAO billingCycleDAO;
    private final MarketDAO marketDAO;
    private final BillingLogDAO billingLogDAO;

    // Not longer require to have defaultBillingPackage,         
    // private BillingPkgVO defaultBillPkg = null;
    //    private static Map<String, BillingPkgVO> billingPkgMap = null;
    public DefaultBillingManager(BillingPkgDAO dao, MarketDAO mkDAO,
                                 BillingCycleDAO cycleDAO, BillingLogDAO billLogDAO) {
        this.billingPkgDAO = dao;
        this.marketDAO = mkDAO;
        this.billingCycleDAO = cycleDAO;
        this.billingLogDAO = billLogDAO;
    }

    @Override
    public void assignMarketAndBillingPackage(UserVO userVO) {
        userVO.setMarketVO(getMarketVOMap().get(DEFAULT_MARKET_NAME));
        BillingPkgVO userDefaulBillingPackage;
        try {
            PromotionVO promotionVO = getPromotionManager().getUserCurrentPromotion(userVO);
            if (promotionVO != null && promotionVO.getFreemiumBillPkgVO() != null) {
                userDefaulBillingPackage = promotionVO.getFreemiumBillPkgVO();
                if (userVO.getPromoteCode() == null) {
                    userVO.setPromoteCode(promotionVO.getPromoteCode());
                    userVO.setPromoteSignUpDate(new Date());
                }
            } else {
                userDefaulBillingPackage = getDefaultBillingPkgMap().get(userVO.getMarketVO().getName());
            }
            userVO.setPackageVO(userDefaulBillingPackage);
            userVO.setCurrentBalance(0);
        } catch (CoreException ex) {
            myLog.trace("Error", ex);
        }
    }

    @Override
    public void assignFreeMiumBillingPackage(UserVO userVO) {
        userVO.setMarketVO(getMarketVOMap().get(DEFAULT_MARKET_NAME));
        BillingPkgVO userDefaulBillingPackage;
        try {
            PromotionVO promotionVO = getPromotionManager().getUserCurrentPromotion(userVO);
            if (promotionVO != null && promotionVO.getFreemiumBillPkgVO() != null) {
                userDefaulBillingPackage = promotionVO.getFreemiumBillPkgVO();
                if (userVO.getPromoteCode() == null) {
                    userVO.setPromoteCode(promotionVO.getPromoteCode());
                    userVO.setPromoteSignUpDate(new Date());
                }
            } else {
                userDefaulBillingPackage = getDefaultFreemiumPkgMap().get(userVO.getMarketVO().getName());
            }
            userVO.setPackageVO(userDefaulBillingPackage);
            userVO.setCurrentBalance(0);
        } catch (CoreException ex) {
            myLog.trace("Error", ex);
        }
    }

    @Override
    public void activateTrialPackageForUser(UserVO userVO) throws CoreException {
        // ensure user have not activate trial before
        myLog.debug("Activating Trial Package for user->" + userVO.getUserid());
        if (userVO.getTrialPackageActivatedAt() != null) {
            throw new CoreException(ErrorType.USER_HAS_ACTIVATED_TRIAL_BEFORE_ERROR, userVO.getLangCode(), userVO.getTrialPackageActivatedAt().toString());
        }
        BillingPkgVO trialPkgVO = getDefaultTrialPkgMap().get(userVO.getMarketVO().getName());
        userVO.setPackageVO(trialPkgVO);
        userVO.setTrialPackageActivatedAt(new Date());
        userVO = getUserDAO().refresh(userVO);
        provisionBillingCycle(trialPkgVO, userVO, 0, Boolean.TRUE);
    }

    @Override
    public BillingPkgVO getDefaultTrialPackage(UserVO userVO) {
        return getDefaultTrialPkgMap().get(userVO.getMarketVO().getName());
    }

    public void verifyUserBillingPackage(UserVO userVO) throws CoreException {
        //JK Feb 15, to cehck if user's latest billing cycle expired, is yes, 
        // reassigned to freemium Package
        myLog.debug("Verifying Billing Packages for user:" + userVO.getUserid() + ", packages is ->" + userVO.getPackageVO().getPkgName());

        UserVO aUserVO = getUserDAO().refresh(userVO);
        BillingCycleVO latestBillCycleVO = getUserLatestBillCycleVO(aUserVO);
        if (new Date().after(latestBillCycleVO.getEndDate())) {
            activateFreeMiumPackage(userVO, true);
        }
    }

    public String getTrialPackageMarketingTextJSON(UserVO userVO) throws CoreException, JSONException {
        BillingPkgVO  trialPkgVO = getDefaultTrialPkgMap().get(userVO.getMarketVO().getName());
        JSONObject paramObj = new JSONObject();
        paramObj.put("PARAM_TRIAL_DAYS", trialPkgVO.getTrialPeriod().toString());
        return getTextUtil().getLocalizedMarketingText(MarketingTextType.ACTIVATE_TRIAL_PACKAGE, userVO.getLangCode(), paramObj);
    }

    public String getPremiumMarketTextConfigJSON(UserVO userVO) throws CoreException, JSONException {
        return getTextUtil().getLocalizedMarketingText(MarketingTextType.PREMIUM_PACKAGE, userVO.getLangCode(), null);
    }

    public void activateFreeMiumPackage(UserVO userVO, Boolean provisionBillCycle) throws CoreException {
        myLog.debug("Activating FreeMium (LITE) Package for user->" + userVO.getUserid());
        BillingPkgVO freemiumPkgVO = null;

        // JK First Check if user entitled for promotion
        PromotionVO promotionVO = getPromotionManager().getUserCurrentPromotion(userVO);

        if (userVO.getPromoteCode() != null && getPromotionManager().getFreemiumBillingPkg(userVO.getPromoteCode()) != null) {
            freemiumPkgVO = getPromotionManager().getFreemiumBillingPkg(userVO.getPromoteCode());
        } else if (promotionVO != null) {
            myLog.debug(userVO.getUserid() + " are entitled for promotion->" + promotionVO.getPromoteCode());
            if (promotionVO.getFreemiumBillPkgVO() != null) {
                freemiumPkgVO = promotionVO.getFreemiumBillPkgVO();
            }
            userVO.setPromoteCode(promotionVO.getPromoteCode());
            userVO.setPromoteSignUpDate(new Date());
            getUserDAO().update(userVO);
        } else {
            freemiumPkgVO =
                    getDefaultFreemiumPkgMap().get(userVO.getMarketVO().getName());
        }

        // TODO needs to assigned base on user PromoteCode;

        userVO.setPackageVO(freemiumPkgVO);
        userVO = getUserDAO().refresh(userVO);
        if (provisionBillCycle) {
        	//change to provision 20 years
            provisionBillingCycle(freemiumPkgVO, userVO, 240, false);
        }
        myLog.debug("Allocated new freemium for user:" + userVO.getUserid() + ", package is->" + userVO.getPackageVO().getPkgName());
    }

    private HashMap<String, MarketVO> getMarketVOMap() {
        if (getCacheUtil().getCachedObject(CacheUtil.MARKET_MAP_KEY) == null) {
            init();
        }
        return (HashMap<String, MarketVO>) getCacheUtil().getCachedObject(CacheUtil.MARKET_MAP_KEY);
    }

    private HashMap<String, BillingPkgVO> getDefaultBillingPkgMap() {
        if (getCacheUtil().getCachedObject(CacheUtil.DEFAULT_BILLING_PKG_KEY) == null) {
            init();
        }
        return (HashMap<String, BillingPkgVO>) getCacheUtil().getCachedObject(CacheUtil.DEFAULT_BILLING_PKG_KEY);
    }

    private HashMap<String, BillingPkgVO> getDefaultFreemiumPkgMap() {
        if (getCacheUtil().getCachedObject(CacheUtil.DEFAULT_FREEMIUM_PKG_KEY) == null) {
            init();
        }
        return (HashMap<String, BillingPkgVO>) getCacheUtil().getCachedObject(CacheUtil.DEFAULT_FREEMIUM_PKG_KEY);
    }

    private HashMap<String, BillingPkgVO> getDefaultTrialPkgMap() {
        if (getCacheUtil().getCachedObject(CacheUtil.DEFAULT_TRIAL_PKG_KEY) == null) {
            init();
        }

        return (HashMap<String, BillingPkgVO>) getCacheUtil().getCachedObject(CacheUtil.DEFAULT_TRIAL_PKG_KEY);
    }

    private HashMap<String, BillingPkgVO> getDefaultPremiumPkgMap() {
        if (getCacheUtil().getCachedObject(CacheUtil.DEFAULT_PREMIUM_PKG_KEY) == null) {
            init();
        }

        return (HashMap<String, BillingPkgVO>) getCacheUtil().getCachedObject(CacheUtil.DEFAULT_PREMIUM_PKG_KEY);
    }
    private void init() {
        HashMap<String, MarketVO> marketVOMap = new HashMap<String, MarketVO>();
        HashMap<String, BillingPkgVO> defaultBillingPkgMap = new HashMap<String, BillingPkgVO>();
        HashMap<String, BillingPkgVO> defaultFreemiumPkgMap = new HashMap<String, BillingPkgVO>();
        HashMap<String, BillingPkgVO> defaultTrialPkgMap = new HashMap<String, BillingPkgVO>();
        HashMap<String, BillingPkgVO> defaultPremiumPkgMap = new HashMap<String, BillingPkgVO>();
        
        List<MarketVO> marketVOList = marketDAO.findALL();
        for (MarketVO marketVO : marketVOList) {
            myLog.debug("Initializing " + marketVO.getName() + " with total of "
                    + marketVO.getBillingPkgList().size() + " billingPackages into hashMap");
            marketVOMap.put(marketVO.getName(), marketVO);

            for (MarketBillingPkgVO marketBillingVO : marketVO.getBillingPkgList()) {
                if (marketBillingVO.isDefaultPkg()) {
                    defaultBillingPkgMap.put(marketVO.getName(), marketBillingVO.getBillingPkgVO());
                }

                if (marketBillingVO.isDefaultFreemium()) {
                    defaultFreemiumPkgMap.put(marketVO.getName(), marketBillingVO.getBillingPkgVO());
                }

                if (marketBillingVO.isDefaultTrialPackage()) {
                    defaultTrialPkgMap.put(marketVO.getName(), marketBillingVO.getBillingPkgVO());
                }
                
                if (marketBillingVO.isDefaultPremium()) {
                	defaultPremiumPkgMap.put(marketVO.getName(), marketBillingVO.getBillingPkgVO());
                }
            }
        }

        getCacheUtil().storeObjectIntoCache(CacheUtil.MARKET_MAP_KEY, marketVOMap);
        getCacheUtil().storeObjectIntoCache(CacheUtil.DEFAULT_BILLING_PKG_KEY, defaultBillingPkgMap);
        getCacheUtil().storeObjectIntoCache(CacheUtil.DEFAULT_FREEMIUM_PKG_KEY, defaultFreemiumPkgMap);
        getCacheUtil().storeObjectIntoCache(CacheUtil.DEFAULT_TRIAL_PKG_KEY, defaultTrialPkgMap);
        getCacheUtil().storeObjectIntoCache(CacheUtil.DEFAULT_PREMIUM_PKG_KEY, defaultPremiumPkgMap);
    }

    public int getAvailableSMSCredit(SecqMeEventVO eventVO) throws CoreException {
        return this.getAvailableSMSCredit(eventVO.getUserVO());
    }

    public int getAvailableSMSCredit(UserVO userVO) throws CoreException {
        int availableSMSCredit = 0;
        BillingCycleVO usrBillCycleVO =
                billingCycleDAO.getUserLatestBillingCycleVO(userVO.getUserid());

        if (usrBillCycleVO.getAllocateSMSCredit() != -1) {
            availableSMSCredit =
                    usrBillCycleVO.getAllocateSMSCredit() - usrBillCycleVO.getTotalSMSCreditUsed();
        } else {
            // means user has signup unlimited SMS Credits 
            // TODO, needs to recheck this logic, always return 999 as for now 
            availableSMSCredit = 999;
        }

        return availableSMSCredit;
    }

    public void chargeSMSCreditUsed(SecqMeEventVO eventVO, int smsCreditUsed) throws CoreException {
        myLog.info("Charging SMS Credit used for user:" + eventVO.getUserVO().getUserid()
                + ", total Credit:" + smsCreditUsed);
        BillingCycleVO usrBillCycleVO =
                billingCycleDAO.getUserLatestBillingCycleVO(eventVO.getUserVO().getUserid());
        chargeEventUsage(eventVO, smsCreditUsed);
        usrBillCycleVO.setTotalSMSCreditUsed(usrBillCycleVO.getTotalSMSCreditUsed() + smsCreditUsed);
        billingCycleDAO.update(usrBillCycleVO);
        insertNewBillingLog(eventVO.getUserVO(), eventVO, usrBillCycleVO, smsCreditUsed, 0,
                BillingLogType.TRANSACTION, "SMS Credits used for notification");

    }

    public void authorizeTransaction(SecqMeEventVO eventVO) throws CoreException {

        // Attempt to find user's latest billing Cycle, and authorize the transactions
        myLog.info("Authorizing transaction for user:" + eventVO.getUserVO().getUserid());
        BillingCycleVO usrBillCycleVO =
                billingCycleDAO.getUserLatestBillingCycleVO(eventVO.getUserVO().getUserid());
        authorizeEventRegistration(usrBillCycleVO, eventVO);
        myLog.info("Event Registration is granted for user:" + eventVO.getUserVO().getUserid());

    }

    public void chargeEventRegistration(SecqMeEventVO eventVO) throws CoreException {

        myLog.info("Charging event registration for user:" + eventVO.getUserVO().getUserid());
        BillingCycleVO usrBillCycleVO =
                billingCycleDAO.getUserLatestBillingCycleVO(eventVO.getUserVO().getUserid());
        chargeEventUsage(eventVO, 0);
        usrBillCycleVO.setTotalEventCreditUsed(usrBillCycleVO.getTotalEventCreditUsed() + 1);
        billingCycleDAO.update(usrBillCycleVO);
        insertNewBillingLog(eventVO.getUserVO(), eventVO, usrBillCycleVO, 0, 1,
                BillingLogType.TRANSACTION, "Event Registration or Emergency Request");


    }

    @Override
    public void provisionBillingCycleForNewUser(BillingPkgVO billPkgVO, UserVO newUserVO) throws CoreException {
    	//change to provision 20 years
        provisionBillingCycle(billPkgVO, newUserVO, 240, Boolean.FALSE);

    }

    @Override
    public void provisionBillingCycle(BillingPkgVO billPkgVO, UserVO userVO, Integer numberOfMonths,
                                      Boolean provisionForTrial) throws CoreException {
        provisionBillingCycle(billPkgVO, userVO, numberOfMonths, provisionForTrial, BillingLogType.PROVISION);
    }

    private void provisionBillingCycle(BillingPkgVO billPkgVO, UserVO userVO, Integer numberOfMonths,
                                       Boolean provisionForTrial, BillingLogType billingLogType) throws CoreException {
        myLog.info("Provision new BillingCycle for user:" + userVO.getUserid()
                + ",market:" + userVO.getMarketVO().getName()
                + ", billingPackage" + billPkgVO.getPkgName()
                + ", qty->" + numberOfMonths);
        BillingCycleVO billCycleVO;
        userVO.setPackageVO(billPkgVO);

        BillingCycleVO userLatestBillCycleVO = getUserLatestBillCycleVO(userVO);
        if (userLatestBillCycleVO != null &&
                userLatestBillCycleVO.getBillingPkgVO().getPkgName().equalsIgnoreCase(billPkgVO.getPkgName()) &&
                !provisionForTrial) {
            // Since it is same bill Package, we will just extends user's current BillCycle;
            Date todayDate = new Date();
            Date newEndDate = DateUtils.addMonths(userLatestBillCycleVO.getEndDate(), numberOfMonths);
            if(todayDate.after(newEndDate)) {
                newEndDate = DateUtils.addMonths(todayDate, numberOfMonths);
            }
            billCycleVO = extendCurrentBillCycle(userLatestBillCycleVO, newEndDate, numberOfMonths);
        } else {
            if (provisionForTrial) {
                billCycleVO = createNewBillCycleWithTrial(userVO);
            } else {
                billCycleVO = createNewBillCycleByMonth(userVO, numberOfMonths);
            }

            myLog.debug("BillCycleVO->" + billCycleVO);
            billingCycleDAO.create(billCycleVO);
            myLog.debug("Billing Log Inserted for->" + userVO.getUserid());
        }

        String billingLogRemark = "BillPkg:" + billPkgVO.getPkgName() + " Months:" + numberOfMonths;
        insertNewBillingLog(userVO, null, billCycleVO, 0, 0, billingLogType, billingLogRemark);
        getUserDAO().update(userVO);
    }

    public BillingCycleVO getUserLatestBillCycleVO(UserVO userVO) {
        return billingCycleDAO.getUserLatestBillingCycleVO(userVO.getUserid());
    }

    public void executeReferralRewards(UserVO orgUserVO, UserVO refUserVO, ReferralProgramVO referralProgramVO) throws CoreException {
        myLog.debug("Rewarding for user->" + orgUserVO.getUserid() + ", refUserVO->" + refUserVO.getUserid() + ", referralProgramVO->" + referralProgramVO.getReferralCode());
        if (referralProgramVO.getOrgUpgradeBillPkg() != null) {
            rewardUser(orgUserVO, referralProgramVO.getOrgUpgradeBillPkg(), referralProgramVO.getOrgBillCycleDaysExtension(), referralProgramVO.getOrgSMSAddition(), BillingLogType.REFERRAL);
        }
        if (referralProgramVO.getReferrerBillPkg() != null) {
            rewardUser(refUserVO, referralProgramVO.getReferrerBillPkg(), referralProgramVO.getReferrerBillCycleDays(), referralProgramVO.getReferrerSMSAddition(), BillingLogType.PROMO_CODE);
        }
    }
    
    public void addSMSCreditToUser(UserVO userVO, int smsCredit) throws CoreException {
    	BillingCycleVO billingCycleVO = billingCycleDAO.getUserLatestBillingCycleVO(userVO.getUserid());
    	int smsAvailable = billingCycleVO.getAllocateSMSCredit();
    	if (smsAvailable >= 0) {
    		billingCycleVO.setAllocateSMSCredit(smsAvailable + smsCredit);
    		billingCycleDAO.update(billingCycleVO);
            insertNewBillingLog(userVO, null, billingCycleVO, 0, 0,
                    BillingLogType.ADDSMSCREDIT, "" + smsCredit + " SMS Credits Added");
    	}
    	
    }
    
    public void checkTrialBillingPackage(UserVO userVO) throws CoreException {
    	BillingCycleVO billingCycleVO = billingCycleDAO.getUserLatestBillingCycleVO(userVO.getUserid());
    	if(billingCycleVO.getBillingPkgVO().getPkgType() == BillingPkgType.TRIAL) {
    		activateFreeMiumPackage(userVO, true);
    		addSMSCreditToUser(userVO, 7);
    	}
    }
    
    public void freePremium(UserVO userVO, int months) throws CoreException {
    	BillingPkgVO premiumBillingPkgVO = getDefaultPremiumPkgMap().get(userVO.getMarketVO().getName());
    	provisionBillingCycle(premiumBillingPkgVO, userVO, months, false);
    }
    
    public void rewardUser(UserVO userVO, BillingPkgVO billingPkgVO, Integer extendedDays, Integer smsAddition, BillingLogType logType) throws CoreException {
        BillingCycleVO billCycleVO = null;
        myLog.debug("RewardReferralUser->userVO:" + userVO.getUserid() +
                ", userCurrentBillPkg->" + userVO.getPackageVO().getPkgName() +
                ", upgradeBillPkg->" + billingPkgVO.getPkgName() +
                ", totalDaysExtended->" + extendedDays +
                ", smsAddition->" + smsAddition);
        if (userVO.getPackageVO().getPkgName().equalsIgnoreCase(billingPkgVO.getPkgName())) {
        	
        	if(BillingPkgType.LITE == userVO.getPackageVO().getPkgType()) {
        		// Add free sms to current bill cycle
        		addSMSCreditToUser(userVO, smsAddition);
        		billCycleVO = getUserLatestBillCycleVO(userVO);
        	} else if (BillingPkgType.PREMIUM == userVO.getPackageVO().getPkgType()) {
        		// Extend the current Bill Cycle
                BillingCycleVO userLatestBillCycle = getUserLatestBillCycleVO(userVO);
                Date endDate = DateUtils.addDays(userLatestBillCycle.getEndDate(), extendedDays);
                billCycleVO = extendCurrentBillCycle(userLatestBillCycle, endDate, 1);
        	}
            
        } else {
        	// TODO: if ref user is alerady premium, do nothing for now
        	if(BillingPkgType.PREMIUM == userVO.getPackageVO().getPkgType()) {
        		
        	} else {
                if (extendedDays != null) {
                    // is upgraded to a better package
                    userVO.setPackageVO(billingPkgVO);
                    getUserDAO().update(userVO);
                    Date startDate = new Date();
                    Date endDate = DateUtils.addDays(startDate, extendedDays);
                    BillingCycleVO newBillCycleVO = createNewBillCycle(userVO, startDate, endDate, 1);
                    billingCycleDAO.create(newBillCycleVO);
                    billCycleVO = newBillCycleVO;
                }
        	}
        }

        insertNewBillingLog(userVO, null, billCycleVO, 0, 0, logType, "Reward to user->" + userVO.getUserid() +
                ", billPkg->" + billingPkgVO.getPkgName() + ", days->" + extendedDays);
    }



    /*
      Moving all the  subscription bill helper methods here, no point of doing so much complicated things
     */

    private void authorizeEventRegistration(BillingCycleVO billCycleVO, SecqMeEventVO secqMeEventVO) throws CoreException {
        // First of all, check if today is still within the billCycle StartDate and EndDate

        // JK May 21, for Good will, we allows over period of one day1
        UserVO userVO = billCycleVO.getUserVO();
        Date today = new Date();
        Date billingEndDate = DateUtils.addDays(billCycleVO.getEndDate(), 1);
        if (today.after(billingEndDate)) {
            // Todo, assign to LITE Package
            myLog.debug(userVO.getUserid() + "'s billPkg:" + billCycleVO.getBillingPkgVO().getPkgName() +
                    " expired at " + billCycleVO.getEndDate() + ", provisioning LITE Package for users");

            BillingPkgVO freemiumPkgVO =
                    getDefaultFreemiumPkgMap().get(billCycleVO.getUserVO().getMarketVO().getName());


            userVO.setPackageVO(freemiumPkgVO);
            userVO = getUserDAO().refresh(userVO);
            provisionBillingCycle(freemiumPkgVO, userVO, 1, false);
            myLog.debug("Allocated new LITE Package for user:" + userVO.getUserid());

        }

        // if allocated credit for event Registration is -1 means the package allows unlimited
        // event registration,
        if ((billCycleVO.getAllocateEventRegCredit() != -1) &&
                (billCycleVO.getAllocateEventRegCredit() <= billCycleVO.getTotalEventCreditUsed())) {
            Integer currentEventCreditBalance = billCycleVO.getAllocateEventRegCredit() -
                    billCycleVO.getTotalEventCreditUsed();

            throw new CoreException(ErrorType.BILLING_INSUFFICIENT_EVENT_CREDIT_ERROR, billCycleVO.getUserVO().getLangCode(),
                    currentEventCreditBalance.toString());
        }
    }

//    private BillingCycleVO provisionBillCycleForTrial(UserVO userVO) throws CoreException {
//        myLog.debug("Creating new BillingCycle(Trial) for user:" + userVO.getUserid() +
//                " billingPkg:" + userVO.getPackageVO().getPkgName() +
//                " trialPeriod:" + userVO.getPackageVO().getTrialPeriod());
//
//        BillingCycleVO billingCycleVO = new BillingCycleVO();
//        billingCycleVO.setUserVO(userVO);
//        billingCycleVO.setBillingPkgVO(userVO.getPackageVO());
//        billingCycleVO.setMarketVO(userVO.getMarketVO());
//
//        Date startDate = new Date();
//        Date endDate = DateUtils.addDays(startDate, userVO.getPackageVO().getTrialPeriod());
//        billingCycleVO.setStartDate(startDate);
//        billingCycleVO.setEndDate(endDate);
//
//
//        // -1 in Event Reg Credit/SMS credit means unlimited,
//        int eventRegCredits = userVO.getPackageVO().getEventRegCredit() == -1 ? -1 :
//                userVO.getPackageVO().getEventRegCredit();
//
//        int smsRegCredits = userVO.getPackageVO().getSmsCredit() == -1 ? -1 :
//                userVO.getPackageVO().getSmsCredit();
//        billingCycleVO.setAllocateEventRegCredit(eventRegCredits);
//        billingCycleVO.setAllocateSMSCredit(smsRegCredits);
//
//        return billingCycleVO;
//    }

    private BillingCycleVO createNewBillCycleByMonth(UserVO userVO, Integer numberOfMonth) throws CoreException {
        Date startDate = new Date();
        Date endDate;
        if (!userVO.getPackageVO().isRenewable() &&
                userVO.getPackageVO().getTrialPeriod() > 0) {
            endDate = DateUtils.addDays(startDate, userVO.getPackageVO().getTrialPeriod());
        } else {
            endDate = DateUtils.addMonths(startDate, numberOfMonth);
        }
        return createNewBillCycle(userVO, startDate, endDate, numberOfMonth);
    }

    private BillingCycleVO createBillingCycleByDays(UserVO userVO, Integer numberOfDay) throws CoreException {
        Date startDate = new Date();
        Date endDate = DateUtils.addDays(startDate, numberOfDay);
        return createNewBillCycle(userVO, startDate, endDate, 1);
    }

    private BillingCycleVO createNewBillCycleWithTrial(UserVO userVO) throws CoreException {
        Date startDate = new Date();
        Date endDate = DateUtils.addDays(startDate, userVO.getPackageVO().getTrialPeriod());
        return createNewBillCycle(userVO, startDate, endDate, 1);

    }

    private BillingCycleVO createNewBillCycle(UserVO userVO, Date startDate, Date endDate, Integer billCycleQTY) throws CoreException {
        myLog.debug("Creting new BillingCycle for user:" + userVO.getUserid() +
                " billingPkg:" + userVO.getPackageVO().getPkgName() +
                " trialPeriod:" + userVO.getPackageVO().getTrialPeriod() +
                " startDate:" + startDate +
                ", endDate:" + endDate);
        BillingCycleVO billingCycleVO = new BillingCycleVO();
        billingCycleVO.setUserVO(userVO);
        billingCycleVO.setBillingPkgVO(userVO.getPackageVO());
        billingCycleVO.setMarketVO(userVO.getMarketVO());

        billingCycleVO.setStartDate(startDate);
        billingCycleVO.setEndDate(endDate);

        int billQty = billCycleQTY == null ? 1 : billCycleQTY;

        // -1 in Event Reg Credit/SMS credit means unlimited,
        int eventRegCredits = userVO.getPackageVO().getEventRegCredit() == -1 ? -1 :
                userVO.getPackageVO().getEventRegCredit() * billQty;

        int smsRegCredits = userVO.getPackageVO().getSmsCredit() == -1 ? -1 :
                userVO.getPackageVO().getSmsCredit() * billQty;
        billingCycleVO.setAllocateEventRegCredit(eventRegCredits);
        billingCycleVO.setAllocateSMSCredit(smsRegCredits);

        return billingCycleVO;
    }

    private BillingCycleVO extendCurrentBillCycle(BillingCycleVO billCycleVO, Date endDate, Integer billCycleQty) throws CoreException {
        myLog.debug("Extending billCycle for->" + billCycleVO.getUserVO().getUserid() + ", new EndDate->" + endDate);
        BillingPkgVO billPkgVO = billCycleVO.getBillingPkgVO();

        billCycleVO.setEndDate(endDate);
        int billQty = billCycleQty == null ? 1 : billCycleQty;
        int eventRegCredits = billPkgVO.getEventRegCredit() == -1 ? -1 :
                billCycleVO.getAllocateEventRegCredit() +
                        (billPkgVO.getEventRegCredit() * billQty);

        int smsRegCredits = billPkgVO.getSmsCredit() == -1 ? -1 :
                billCycleVO.getAllocateSMSCredit() +
                        (billPkgVO.getSmsCredit() * billQty);
        billCycleVO.setAllocateEventRegCredit(eventRegCredits);
        billCycleVO.setAllocateSMSCredit(smsRegCredits);
        billingCycleDAO.update(billCycleVO);
        return billCycleVO;
    }

    private void chargeEventUsage(SecqMeEventVO secqMeEventVO, int smsCreditUsed) throws CoreException {
    }

    private void insertNewBillingLog(UserVO userVO, SecqMeEventVO eventVO,
                                     BillingCycleVO billCycleVO, int smsCreditUsed,
                                     int eventRegCreditUsed, BillingLogType logType,
                                     String billLogRemark) {
        BillingLogVO billingLogVO = new BillingLogVO();
        billingLogVO.setUserVO(userVO);
        billingLogVO.setSecqMeEventVO(eventVO);
        billingLogVO.setBillingCycleVO(billCycleVO);
        billingLogVO.setSmsCreditUsed(smsCreditUsed);
        billingLogVO.setEventRegCreditUsed(eventRegCreditUsed);
        billingLogVO.setLogTime(new Date());
        billingLogVO.setLogType(logType);
        billingLogVO.setRemark(billLogRemark);
        this.billingLogDAO.create(billingLogVO);
    }
}

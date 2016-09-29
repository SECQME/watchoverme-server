package com.secqme.manager.payment;

import java.util.*;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.secqme.CoreException;
import com.secqme.domain.dao.GiftPaymentLogDAO;
import com.secqme.domain.dao.PaymentClickDAO;
import com.secqme.domain.dao.PaymentGWDAO;
import com.secqme.domain.dao.PaymentHistoryDAO;
import com.secqme.domain.dao.PricingPackageDAO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.domain.model.payment.PaymentClickVO;
import com.secqme.domain.model.payment.PaymentGWVO;
import com.secqme.domain.model.payment.PaymentHistoryVO;
import com.secqme.domain.model.payment.PaymentType;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.payment.UserPaymentInfoVO;
import com.secqme.manager.BaseManager;
import com.secqme.manager.billing.BillingManager;
import com.secqme.util.payment.PaymentGateway;
import com.secqme.util.payment.PaymentHelper;

/**
 *
 * @author coolboykl
 */
public class DefaultPaymentManager extends BaseManager implements PaymentManager {

    private static final Logger myLog = Logger.getLogger(DefaultPaymentManager.class);
    private PaymentGWDAO paymentGWDAO = null;
    private PaymentHistoryDAO paymentHistoryDAO = null;
    private PricingPackageDAO pricingPackageDAO = null;
    private BillingManager billingManager = null;
    private PaymentHelper paymentHelper = null;
    private GiftPaymentLogDAO giftPaymentLogDAO = null;
    private PaymentClickDAO paymentClickDAO = null;
    private static final String AUTO_RENEW_KEY = "auto_renew";
    private static final String ONE_TIME_PAYMENT_KEY = "one_time_payment";
    //TODO needs to refactor;
    private static final String DEFAULT_PRICE_PKG_CODE = "PKG_DF_1M";
    private static PricingPackageVO defaultPricePkgVO = null;
    //TODO: use default market and language for user purchase from web temporary
    private static final String DEFAULT_MARKET = "default";
    private static final String DEFAULT_LANGUAGE = "en_US";
    
    public DefaultPaymentManager(PaymentGWDAO gwDAO, PaymentHistoryDAO paymntHistDAO,
            PricingPackageDAO pricePkgDAO, BillingManager billingManager,
            PaymentHelper pymUtil, GiftPaymentLogDAO giftPaymentLogDAO,
            PaymentClickDAO paymentClickDAO) {
        this.paymentGWDAO = gwDAO;
        this.paymentHistoryDAO = paymntHistDAO;
        this.pricingPackageDAO = pricePkgDAO;
        this.billingManager = billingManager;
        this.paymentHelper = pymUtil;
        defaultPricePkgVO = pricePkgDAO.read(DEFAULT_PRICE_PKG_CODE);
        this.giftPaymentLogDAO = giftPaymentLogDAO;
        this.paymentClickDAO = paymentClickDAO;
    }

    public List<PricingPackageVO> getAutoRenewPricingPackage(String marketCode) {
        return getActivePricingPackage(marketCode, Boolean.TRUE);
    }

    public List<PricingPackageVO> getOneTimePaymentPricingPackage(String marketCode) {
        return getActivePricingPackage(marketCode, Boolean.FALSE);
    }

    private List<PricingPackageVO> getActivePricingPackage(String marketCode, Boolean autoRenew) {
        List<PricingPackageVO> pricingPkgList = null;
        List<PricingPackageVO> thePricingPkgList = null;
        String cacheKey = autoRenew ? AUTO_RENEW_KEY : ONE_TIME_PAYMENT_KEY;
        myLog.debug("Retriving all the  Pricing Package for Market :" + marketCode + "AutoRenew->" + autoRenew);
        if (getCacheUtil().getCachedObject(getCacheUtil().PRICING_PKG_KEY + marketCode + cacheKey) == null) {
            pricingPkgList = pricingPackageDAO.findAllActivePackageWithOutPromotion(marketCode);
            if (pricingPkgList != null) {
                thePricingPkgList = new ArrayList<PricingPackageVO>();
                for (PricingPackageVO pricePkgVO : pricingPkgList) {
                    if (pricePkgVO.isAutoRenew() == autoRenew) {
                        thePricingPkgList.add(pricePkgVO);
                    }
                }
                myLog.debug("Storing total of " + thePricingPkgList.size() + " PricingPkges into Cached, autoRenew->" + autoRenew);
                getCacheUtil().storeObjectIntoCache(getCacheUtil().PRICING_PKG_KEY + marketCode + cacheKey, thePricingPkgList);
            }
        } else {
            thePricingPkgList = (List<PricingPackageVO>) getCacheUtil().getCachedObject(getCacheUtil().PRICING_PKG_KEY + marketCode + cacheKey);
        }

        return thePricingPkgList;
    }

    private HashMap<String, PaymentGateway> getPaymentGWHashMap() {
        if (getCacheUtil().getCachedObject(getCacheUtil().PAYMENT_GW_HASH_MAP_KEY) == null) {
            initAllPaymentGW();
        }
        return (HashMap<String, PaymentGateway>) getCacheUtil().getCachedObject(getCacheUtil().PAYMENT_GW_HASH_MAP_KEY);
    }

    @Override
    public JSONObject processInitPayment(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String invCode, Double price, UserVO userVO, String paymentGWName) throws CoreException {
        JSONObject jobj = null;
        if (userVO != null && invCode != null && getPaymentGWHashMap().get(paymentGWName) != null) {
            jobj = getPaymentGWHashMap().get(paymentGWName).initPaymentTransaction(paymentType, autoRenew, pkgDesc, invCode, price, userVO);
        }
        return jobj;
    }

    @Override
    public JSONObject processInitPaymentForGifting(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String invCode, Double price, GiftPaymentLogVO giftPaymentLogVO, String paymentGWName) throws CoreException {
    	JSONObject jobj = null;
        if (giftPaymentLogVO != null && invCode != null && getPaymentGWHashMap().get(paymentGWName) != null) {
            jobj = getPaymentGWHashMap().get(paymentGWName).initPaymentTransactionForGifting(paymentType, autoRenew, pkgDesc, invCode, price, giftPaymentLogVO);
        }
        return jobj;
    }
    
    @Override
    public void chargeFirstTimeSubscription(PaymentType paymentType, PricingPackageVO pricePkgVO,
            String userToken, JSONObject sessionObj, String gwName) throws CoreException {
            UserVO userVO = getUserDAO().findByActivationCode(userToken);

        if (userVO != null && getPaymentGWHashMap().get(gwName) != null) {
            BillingPkgVO billPkgVO = null;
            Integer billCycles = 0;
            
            if(PaymentType.NEW_SUBSCRIPTION.equals(paymentType)) {
                billPkgVO = pricePkgVO.getBillPkgVO();
                billCycles = pricePkgVO.getQuantity();
            }
                    
            
            myLog.debug("Process Payment for: " + userVO.getUserid() + ", for PaymentGW->" + gwName
                         + ", paymentType:" + paymentType.name());
            PaymentHistoryVO paymentRecord =
                    getPaymentGWHashMap().get(gwName).processPaymentTransaction(paymentType, pricePkgVO, 
                                                                                userVO, sessionObj);
            paymentHistoryDAO.create(paymentRecord);

            subscribePricingPackage(billPkgVO, billCycles, userVO);
        }
    }
    

    @Override
    public void updateUserPaymentInfo(String userToken, JSONObject sessionObj, String paymentGWName) throws CoreException {
        UserVO userVO = getUserDAO().findByActivationCode(userToken);
        if (userVO != null && getPaymentGWHashMap().get(paymentGWName) != null) {
            myLog.debug("Update userPayment Info for : " + userVO.getUserid() + ", for PaymentGW->" + paymentGWName);
            UserPaymentInfoVO usrPaymentInfo =
                    getPaymentGWHashMap().get(paymentGWName).retrieveUserPaymentInfo(userVO, sessionObj);

            if (usrPaymentInfo != null) {
                myLog.debug("Adding new PaymentInfo->" + usrPaymentInfo.getPaymentid() + " for user:" + userVO.getUserid());
                // Check if existing user have the same paymentInfo record
                if (userVO.getUserPaymentInfoList() != null && userVO.getUserPaymentInfoList().size() > 0) {
                    for (UserPaymentInfoVO paymentInfo : userVO.getUserPaymentInfoList()) {
                        if (paymentInfo.getGwName().equals(usrPaymentInfo.getGwName())) {
                            // Needs to remove the old payment Info Record with
                            // the new one
                        	myLog.debug("Removing old PaymentInfo->" + paymentInfo.getPaymentid());
                            userVO.getUserPaymentInfoList().remove(paymentInfo);
                            getUserDAO().update(userVO);
                            break;
                        }
                    }
                } else {
                    List<UserPaymentInfoVO> paymentInfoList = new ArrayList<UserPaymentInfoVO>();
                    userVO.setUserPaymentInfoList(paymentInfoList);
                }
                userVO.getUserPaymentInfoList().add(usrPaymentInfo);
                getUserDAO().update(userVO);
                myLog.debug("User Payment Info updated");
            }
        }
    }

    public UserPaymentInfoVO getUserPaymentInfo(JSONObject sessionObj, String paymentGWName) throws CoreException {
    	UserPaymentInfoVO usrPaymentInfo = null;
    	if(getPaymentGWHashMap().get(paymentGWName) != null) {
    		usrPaymentInfo =
                    getPaymentGWHashMap().get(paymentGWName).retrieveUserPaymentInfo(null, sessionObj);
    	}
    	
        return usrPaymentInfo;
    }
    
    public void subscribePricingPackage(BillingPkgVO billPkgVO, Integer billCycles, UserVO userVO) throws CoreException {
        myLog.debug("Subscribing new billPkgVO Package" + billPkgVO + " for->" + userVO.getUserid());
        billingManager.provisionBillingCycle(billPkgVO, userVO, billCycles, false);
    }



    private void initAllPaymentGW() {
        List<PaymentGWVO> paymentGWList = paymentGWDAO.findAll();
        if (paymentGWList != null && paymentGWList.size() > 0) {
            myLog.debug("Initializing total of " + paymentGWList.size() + " Payment GW");
            HashMap<String, PaymentGateway> paymentGWHashMap = new HashMap<String, PaymentGateway>();
            PaymentGateway payGW = null;
            for (PaymentGWVO payGWVO : paymentGWList) {
                try {
                    myLog.debug("Configuring " + payGWVO.getGwName() + ", " + payGWVO.getDescription() + " Gateway");
                    payGW = (PaymentGateway) Class.forName(payGWVO.getImplClass()).newInstance();
                    payGW.initPaymentGW(payGWVO, paymentHelper);
                    paymentGWHashMap.put(payGWVO.getGwName(), payGW);
                } catch (InstantiationException ex) {
                    myLog.error("Problem of Instantiate " + payGWVO.getGwName()
                            + ", implClass:" + payGWVO.getImplClass() + " error->" + ex.getMessage(), ex);
                } catch (IllegalAccessException ex) {
                    myLog.error("IllegalAccessException " + payGWVO.getGwName()
                            + ", implClass:" + payGWVO.getImplClass() + " error->" + ex.getMessage(), ex);
                } catch (ClassNotFoundException ex) {
                    myLog.error("Class Not Found for " + payGWVO.getGwName()
                            + ", implClass:" + payGWVO.getImplClass() + " error->" + ex.getMessage(), ex);
                }
            }
            getCacheUtil().storeObjectIntoCache(getCacheUtil().PAYMENT_GW_HASH_MAP_KEY, paymentGWHashMap);
        }

    }

    public JSONObject getPaymentGWConfig(String gwName) {
        JSONObject obj = null;
        PaymentGateway gw = getPaymentGWHashMap().get(gwName);
        if (gw != null) {
            obj = gw.getPaymentGWConfig();
        }
        return obj;
    }

    public PaymentGateway getPaymentGW(String gwName) {
        return getPaymentGWHashMap().get(gwName);
    }

    public void handleGWNotification(String userToken, String pkgCode, JSONObject notificationObj, String paymentGWName) throws CoreException {
        PaymentHistoryVO paymentRecord = null;
        UserVO userVO = getUserDAO().findByActivationCode(userToken);
        PricingPackageVO pricePkgVO = pricingPackageDAO.read(pkgCode);

        // TODO 
        // James Nov 15
        // This is just temporary, of migrating all Econimic Price Package to 
        // Personal Gold

        if (pricePkgVO == null) {
            pricePkgVO = defaultPricePkgVO;
        }

        if (userVO != null
                && pricePkgVO != null
                && getPaymentGWHashMap().get(paymentGWName) != null) {
            paymentRecord = getPaymentGWHashMap()
                    .get(paymentGWName)
                    .handleGatewayNotification(userVO, pricePkgVO, notificationObj);
        }

        if (paymentRecord != null) {
            // means receivePayment, do somethings..
            paymentHistoryDAO.create(paymentRecord);
            // Check if recurring Payment
            switch (PaymentType.valueOf(paymentRecord.getPaymentType())) {
                case SUBSCRIPTION_RENEW:
                    myLog.debug("Yes, receiving subscription payment of " + paymentRecord.getPaymentAmt()
                            + " from user: " + userVO.getUserid());
                    billingManager.provisionBillingCycle(pricePkgVO.getBillPkgVO(), userVO, pricePkgVO.getQuantity(), false);

                default:
                    break;
            }
        }
    }

    public void handleGWNotificationGifting(CountryVO countryVO, String mobileNumber, String pkgCode, JSONObject notificationObj, String paymentGWName) throws CoreException {
    	//Store to table and check if recipient already has an account.
    	//if yes, upgrade recipient to premium or extend premium
        PaymentHistoryVO paymentRecord = null;
        UserVO userVO = getUserDAO().findByMobileNumberAndMobileCountry(countryVO.getIso(), mobileNumber);
        PricingPackageVO pricePkgVO = pricingPackageDAO.read(pkgCode);

        if (pricePkgVO == null) {
            pricePkgVO = defaultPricePkgVO;
        }

        if (userVO != null && pricePkgVO != null
                && getPaymentGWHashMap().get(paymentGWName) != null) { //for user that created account
            paymentRecord = getPaymentGWHashMap()
                    .get(paymentGWName)
                    .handleGatewayNotification(userVO, pricePkgVO, notificationObj);
        } else if (pricePkgVO != null
                && getPaymentGWHashMap().get(paymentGWName) != null) { //for user that dont have account yet
        	paymentRecord = getPaymentGWHashMap()
                    .get(paymentGWName)
                    .handleGatewayNotification(countryVO.getCallingCode() + "-" + mobileNumber
                    		, pricePkgVO, notificationObj);
        }

        if (paymentRecord != null) {
            // means receivePayment or user cancel payment, log to paymentHistory
            paymentHistoryDAO.create(paymentRecord);
            if(userVO != null) {
            	// Check if recurring Payment
                switch (PaymentType.valueOf(paymentRecord.getPaymentType())) {
                    case SUBSCRIPTION_RENEW:
                        myLog.debug("Yes, receiving subscription payment of " + paymentRecord.getPaymentAmt()
                                + " from user: " + userVO.getUserid());
                        billingManager.provisionBillingCycle(pricePkgVO.getBillPkgVO(), userVO, pricePkgVO.getQuantity(), false);

                    default:
                        break;
                }
            }
        }
    }
    public List<PaymentHistoryVO> getUserPaymentHistory(String userid) {
        return paymentHistoryDAO.findByUserId(userid);
    }

    public void cancelSubscription(UserVO userVO, String cancelNote) {
        String gwName = userVO.getUserCurrentSubscription().getGwName();
        myLog.debug("Cancelling subscription for " + userVO.getUserid());
        this.getPaymentGWHashMap().get(gwName).cancelSubscription(userVO, cancelNote);
        userVO.setUserCurrentSubscription(null);
        getUserDAO().update(userVO);
    }

    public void chargeFirstTimeGifting(PaymentType paymentType, PricingPackageVO pricePkgVO,
    		 UserVO userVO, JSONObject sessionObj, String gwName, GiftPaymentLogVO giftPaymentLogVO) 
    				 throws CoreException {
    	myLog.debug("chargeFirstTimeGifting " + "*" + paymentType
    			+ "*" + pricePkgVO
    			+ "*" + userVO
    			+ "*" + sessionObj
    			+ "*" + gwName
    			+ "*" + giftPaymentLogVO);
    	
        if(getPaymentGWHashMap().get(gwName) != null) {
        	BillingPkgVO billPkgVO = null;
            Integer billCycles = 0;
            
            if(PaymentType.NEW_SUBSCRIPTION.equals(paymentType)) {
                billPkgVO = pricePkgVO.getBillPkgVO();
                billCycles = pricePkgVO.getQuantity();
            }
            PaymentHistoryVO paymentRecord = null;
        	if (userVO == null) { //new user that is not exist in db
        		myLog.debug("Gifting Process Payment for number: " + giftPaymentLogVO.getRecipientMobileNumber()
        				+ ", for PaymentGW->" + gwName
                        + ", paymentType:" + paymentType.name());
	           paymentRecord =
	                   getPaymentGWHashMap().get(gwName).processPaymentTransactionForGifting
	                   (paymentType, pricePkgVO, sessionObj, giftPaymentLogVO);
	           paymentHistoryDAO.create(paymentRecord);
        	} else { //exisiting user
                myLog.debug("Gifting Process Payment for: " + userVO.getUserid() + ", for PaymentGW->" + gwName
                             + ", paymentType:" + paymentType.name());
                paymentRecord =
                        getPaymentGWHashMap().get(gwName).processPaymentTransaction
                        (paymentType, pricePkgVO, userVO, sessionObj);
                subscribePricingPackage(billPkgVO, billCycles, userVO);
                paymentHistoryDAO.create(paymentRecord);
            }
        	paymentHistoryDAO.refresh(paymentRecord);
        	myLog.debug("Gifting paymenthistory id " + paymentRecord.getId());
        	giftPaymentLogVO.setPaymentHistoryVO(paymentRecord);
        	insertGiftPaymentLog(giftPaymentLogVO, gwName);
        	getNotificationEngine().sendThanksGifting(DEFAULT_MARKET, DEFAULT_LANGUAGE, giftPaymentLogVO);
        }
    	
    }

    @Override
    public void insertGiftPaymentLog(GiftPaymentLogVO giftPaymentLogVO, String paymentGWName) throws CoreException {
    	giftPaymentLogDAO.create(giftPaymentLogVO);
    }

    @Override
    public List<GiftPaymentLogVO> getGiftPackageByEmailAddress(String emailAddress) throws CoreException {
        return giftPaymentLogDAO.findGiftPackageByEmailAddress(emailAddress);
    }

    @Override
    public List<GiftPaymentLogVO> getGiftPackageByMobileCountryAndMobileNumber(String mobileCountry, String mobileNumber) throws CoreException {
    	return giftPaymentLogDAO.findGiftPackageByCountryAndMobileNumber(mobileCountry, mobileNumber);
    }

    @Override
    public List<GiftPaymentLogVO> getGiftPackageForUser(UserVO userVO) throws CoreException {
        List<GiftPaymentLogVO> gifts = new ArrayList<GiftPaymentLogVO>();
        if (userVO.getEmailAddress() != null) {
            List<GiftPaymentLogVO> emailGifts = getGiftPackageByEmailAddress(userVO.getEmailAddress());
            if (emailGifts != null) {
                gifts.addAll(emailGifts);
            }
        }

        if (userVO.getMobileCountry() != null && userVO.getMobileNo() != null) {
            List<GiftPaymentLogVO> mobileGifts = getGiftPackageByMobileCountryAndMobileNumber(userVO.getMobileCountry().getIso(), userVO.getMobileNo());
            if (mobileGifts != null) {
                gifts.addAll(mobileGifts);
            }
        }

        return gifts;
    }
    
    public void updateGiftPaymentLog(GiftPaymentLogVO giftPaymentLogVO) throws CoreException {
    	giftPaymentLogDAO.update(giftPaymentLogVO);
    }
    
    public void insertPaymentClickRequest(UserVO userVO, String device, String packageCode) throws CoreException {
    	PaymentClickVO paymentClickVO = new PaymentClickVO();
    	paymentClickVO.setDevice(device);
    	paymentClickVO.setPackageCode(packageCode);
    	paymentClickVO.setUserid(userVO.getUserid());
    	paymentClickVO.setInsertDate(new Date());
    	paymentClickDAO.create(paymentClickVO);
    }
}

package com.secqme.manager.payment;

import java.util.List;

import org.codehaus.jettison.json.JSONObject;

import com.secqme.CoreException;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.domain.model.payment.PaymentHistoryVO;
import com.secqme.domain.model.payment.PaymentType;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.payment.UserPaymentInfoVO;
import com.secqme.util.payment.PaymentGateway;

/**
 *
 * @author coolboykl
 */
public interface PaymentManager {

    public List<PricingPackageVO> getAutoRenewPricingPackage(String marketCode);

    public JSONObject processInitPayment(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String invCode, Double price, UserVO userVO, String paymentGWName) throws CoreException;

    public void updateUserPaymentInfo(String userToken, JSONObject sessionObj, String paymentGWName) throws CoreException;
    public UserPaymentInfoVO getUserPaymentInfo(JSONObject sessionObj, String paymentGWName) throws CoreException;
//    public void chargeFirstTimeSubscription(String userToken, String pkgCode,
//            JSONObject sessionObj, String paymentGWName) throws CoreException;
    
    public void chargeFirstTimeSubscription(PaymentType paymentType, PricingPackageVO pricePkgVO,
            String userToken, JSONObject sessionObj, String gwName) throws CoreException;

    public JSONObject getPaymentGWConfig(String gwName);

    public void handleGWNotification(String userToken, String pkgCode, JSONObject notificationObj,
            String paymentGWName) throws CoreException;

    public List<PaymentHistoryVO> getUserPaymentHistory(String userid);

    public void cancelSubscription(UserVO userVO, String cancelNote);

    public PaymentGateway getPaymentGW(String gwName);

    public List<PricingPackageVO> getOneTimePaymentPricingPackage(String marketCode);
    
    public JSONObject processInitPaymentForGifting(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String invCode, Double price, GiftPaymentLogVO giftPaymentLogVO, String paymentGWName) throws CoreException;
    
    public void handleGWNotificationGifting(CountryVO countryVO, String mobileNumber, String pkgCode, JSONObject notificationObj,
            String paymentGWName) throws CoreException;
    
    public void chargeFirstTimeGifting(PaymentType paymentType, PricingPackageVO pricePkgVO,
            UserVO userVO, JSONObject sessionObj, String gwName, GiftPaymentLogVO giftPaymentLogVO) throws CoreException;
    
    public void insertGiftPaymentLog(GiftPaymentLogVO giftPaymentLogVO, String paymentGWName) throws CoreException;

    public List<GiftPaymentLogVO> getGiftPackageByEmailAddress(String emailAddress) throws CoreException;

    public List<GiftPaymentLogVO> getGiftPackageByMobileCountryAndMobileNumber(String mobileCountry, String mobileNumber) throws CoreException;

    public List<GiftPaymentLogVO> getGiftPackageForUser(UserVO userVO) throws CoreException;
    
    public void updateGiftPaymentLog(GiftPaymentLogVO giftPaymentLogVO) throws CoreException;
    
    public void insertPaymentClickRequest(UserVO userVO, String device, String packageCode) throws CoreException;
}

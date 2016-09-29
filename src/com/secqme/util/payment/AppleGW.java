package com.secqme.util.payment;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.domain.model.payment.PaymentGWVO;
import com.secqme.domain.model.payment.PaymentHistoryVO;
import com.secqme.domain.model.payment.PaymentType;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.payment.UserPaymentInfoVO;

/**
 * This is just empty implementation, 
 *
 * @author coolboykl
 */
public class AppleGW implements PaymentGateway {

    private static final Logger myLog = Logger.getLogger(AppleGW.class);
    private PaymentGWVO gwVO = null;
    private PaymentHelper paymentHelper = null;

    public AppleGW() {
        // Empty Constructor 
    }

    public void initPaymentGW(PaymentGWVO gwVO, PaymentHelper pymUtil) {
        myLog.debug("Initializing Apple Payment GW:" + gwVO.getDescription());
        this.gwVO = gwVO;
        this.paymentHelper = pymUtil;
    }

    public void cancelSubscription(UserVO userVO, String cancelNote) {
        // NA
    }

    public JSONObject getPaymentGWConfig() {
        // NA
        return null;
    }

    public PaymentHistoryVO handleGatewayNotification(UserVO userVO, PricingPackageVO pricingPkgVO, JSONObject notificationObj) throws CoreException {
        // NA
        return null;
    }

    public JSONObject initPaymentTransaction(PricingPackageVO pricingPkgVO, UserVO userVO) throws CoreException {
        // NA
        return null;
    }

    public UserPaymentInfoVO retrieveUserPaymentInfo(UserVO userVO, JSONObject sessionObj) throws CoreException {
        // NA
        return null;
    }

    public PaymentHistoryVO processFirstTimeSubscription(UserVO userVO, PricingPackageVO pricingPkgVO, JSONObject sessionObj) throws CoreException {
        // NA
        return null;
    }

    public PaymentGWVO getPaymentGWVO() {
        return gwVO;
    }

    @Override
    public JSONObject initPaymentTransaction(PaymentType paymentType, Boolean autoRenew, String pkgDesc, String invCode, Double price, UserVO userVO) throws CoreException {
        return null;
    }

    public JSONObject initPaymentTransactionForGifting(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String productCode, Double price, GiftPaymentLogVO giftPaymentLogVO) throws CoreException {
    	return null;
    }
    
    @Override
    public PaymentHistoryVO processPaymentTransaction(PaymentType paymentType, PricingPackageVO pricePkgVO,
                                                      UserVO userVO, JSONObject sessionObj) throws CoreException {
         return null;
    }
    
    public PaymentHistoryVO processPaymentTransactionForGifting(PaymentType paymentType, PricingPackageVO pricePkgVO,
            JSONObject sessionObj, GiftPaymentLogVO giftPaymentLogVO) throws CoreException {
    	return null;
    }
    
    public PaymentHistoryVO handleGatewayNotification(String giftedUserid, PricingPackageVO pricingPkgVO, 
    		JSONObject notificationObj) throws CoreException {
    	return null;
    }
}

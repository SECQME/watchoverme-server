package com.secqme.util.payment;

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
 *
 * @author coolboykl
 */
public interface PaymentGateway {

    public void initPaymentGW(PaymentGWVO gwVO, PaymentHelper paymentHelper);

    public PaymentGWVO getPaymentGWVO();

    public void cancelSubscription(UserVO userVO, String cancelNote);

    public JSONObject getPaymentGWConfig();

    public PaymentHistoryVO handleGatewayNotification(UserVO userVO, PricingPackageVO pricingPkgVO, JSONObject notificationObj) throws CoreException;

    public JSONObject initPaymentTransaction(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String productCode, Double price, UserVO userVO) throws CoreException;

    public UserPaymentInfoVO retrieveUserPaymentInfo(UserVO userVO, JSONObject sessionObj) throws CoreException;

//    public PaymentHistoryVO processFirstTimeSubscription(UserVO userVO, PricingPackageVO pricingPkgVO, JSONObject sessionObj) throws CoreException;

    public PaymentHistoryVO processPaymentTransaction(PaymentType paymentType, PricingPackageVO pricePkgVO,
            UserVO userVO, JSONObject sessionObj) throws CoreException;
    
    public JSONObject initPaymentTransactionForGifting(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String productCode, Double price, GiftPaymentLogVO giftDetailVO) throws CoreException;
    
    public PaymentHistoryVO processPaymentTransactionForGifting(PaymentType paymentType, PricingPackageVO pricePkgVO,
            JSONObject sessionObj, GiftPaymentLogVO giftPaymentLogVO) throws CoreException;
    
    public PaymentHistoryVO handleGatewayNotification(String giftedUserid, PricingPackageVO pricingPkgVO, JSONObject notificationObj) throws CoreException;
}    
    

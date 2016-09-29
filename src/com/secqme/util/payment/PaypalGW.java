package com.secqme.util.payment;

import com.paypal.sdk.core.nvp.NVPDecoder;
import com.paypal.sdk.core.nvp.NVPEncoder;
import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.sdk.profiles.APIProfile;
import com.paypal.sdk.profiles.ProfileFactory;
import com.paypal.sdk.services.NVPCallerServices;
import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.*;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author coolboykl
 */
public class PaypalGW implements PaymentGateway {

    private static final Logger myLog = Logger.getLogger(PaypalGW.class);
    public final static String USER_NAME_KEY = "user.name";
    public final static String PASSWORD_KEY = "password";
    public final static String API_VERSION_KEY = "api.version";
    public final static String RETURN_URL_KEY = "return.url";
    public final static String CANCEL_URL_KEY = "cancel.url";
    public final static String SIGNATURE_KEY = "signature";
    public final static String API_URL_KEY = "api.url";
    public final static String IPN_URL_KEY = "ipnURL";
    public final static String ENVIRONMENT_KEY = "environment";
    public final static String REDIRECT_URL_KEY = "redirectURL";
    public final static String NVP_METHOD_KEY = "METHOD";
    public final static String NVP_VERSION_KEY = "VERSION";
    public final static String NVP_PAYMENT_AMT_KEY = "PAYMENTREQUEST_0_AMT";
    public final static String NVP_PAYMENT_ACTION_KEY = "PAYMENTREQUEST_0_PAYMENTACTION";
    public final static String NVP_RETURN_URL_KEY = "RETURNURL";
    public final static String NVP_CANCEL_URL_KEY = "CANCELURL";
    public final static String NVP_TOKEN_KEY = "TOKEN";
    public final static String SET_EXPRESS_CHECKOUT_NAME = "SetExpressCheckout";
    public final static String GET_EXPRESS_CHECKOUT_DETAIL_NAME = "GetExpressCheckoutDetails";
    public final static String DO_EXPRESS_CHECKOUT_METHOD_NAME = "DoExpressCheckoutPayment";
    public final static String CREATE_SUBSCRIPTION_PROFILE_METHOD_NAME = "CreateRecurringPaymentsProfile";
    public final static String USER_TOKEN_KEY = "usertoken";
    public final static String PRICE_PKG_CODE_KEY = "pkgcode";
    public static final String PROMOTION_CODE_KEY = "promotecode";
    public static final String PAYMENT_TYPE_KEY = "paymenttype";
    public final static String PAYPAL_PAYER_ID_KEY = "PayerID";
    
    public static final String GIFT_NAME_KEY = "giftname";
    public static final String GIFT_MOBILE_COUNTRY_KEY = "giftmobilecountry";
    public static final String GIFT_MOBILE_NUMBER_KEY = "giftmobilenumber";
    public static final String GIFT_RECIPIENT_NAME_KEY = "giftrecipientname";
    public static final String GIFT_RECIPIENT_MOBILE_COUNTRY_KEY = "giftrecipientmobilecountry";
    public static final String GIFT_RECIPIENT_MOBILE_NUMBER_KEY = "giftrecipientmobilenumber";
    public static final String GIFT_RECIPIENT_EMAIL_KEY = "giftrecipientemail";
    public static final String GIFT_EMAIL = "giftemail";
    public static final String GIFT_MESSAGE = "giftmessage";
    public static final String GIFT_SUBSCRIBE = "giftsubscribe";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat paypalDateFormat =
            new SimpleDateFormat("HH:mm:ss MMM d, yyyy z", Locale.ENGLISH);
    private static String apiUserName = null;
    private static String apiUserPassword = null;
    private static String apiVersion = null;
    private static String returnURL = null;
    private static String cancelURL = null;
    private static String apiSignature = null;
    private static String environment = null;
    private static String paypalRedirectURL = null;
    private static APIProfile apiProfile = null;
    private static NVPCallerServices payPalService = null;
    private PaymentGWVO gwVO = null;
    private PaymentHelper paymentHelper = null;

    public PaypalGW() {
        // Empty Constructor 
    }

    public void initPaymentGW(PaymentGWVO gwVO, PaymentHelper pymUtil) {
        myLog.debug("Initializing Paypal Payment GW:" + gwVO.getDescription());
        this.gwVO = gwVO;
        this.paymentHelper = pymUtil;
        JSONObject obj = gwVO.getConfig();
        if (obj != null) {
            try {
                apiUserName = obj.getString(USER_NAME_KEY);
                apiUserPassword = obj.getString(PASSWORD_KEY);
                apiVersion = obj.getString(API_VERSION_KEY);
                returnURL = obj.getString(RETURN_URL_KEY);
                cancelURL = obj.getString(CANCEL_URL_KEY);
                apiSignature = obj.getString(SIGNATURE_KEY);
                environment = obj.getString(ENVIRONMENT_KEY);

                paypalRedirectURL = obj.getString(REDIRECT_URL_KEY);
                initPayPalService();
            } catch (PayPalException ex) {
                myLog.error(ex.getMessage(), ex);
            } catch (JSONException ex) {
                myLog.error("Problem to init Paypal Payment GW:" + gwVO.getDescription()
                        + ", msg->" + ex.getMessage(), ex);
            }

        }
    }

    @Override
    public JSONObject initPaymentTransaction(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String invCode, Double price, UserVO userVO) throws CoreException {
        JSONObject sessionObj = new JSONObject();
        try {
            myLog.debug("Initialize payment transaction for:" + userVO.getUserid()
                    + ", paymentType:" + paymentType.name()
                    + ", pkg or promote Code:" + invCode
                    + ", price->" + price);
            NVPDecoder decoder = new NVPDecoder();
            String strNVPString = prepareInitPaymentReqString(paymentType, autoRenew, pkgDesc, invCode, price, userVO);
            myLog.debug("Encoded request->" + strNVPString);

            String strNVPResponse =
                    (String) payPalService.call(strNVPString);
            myLog.debug("PayPal setExpressCheckout Response->" + strNVPResponse);
            decoder.decode(strNVPResponse);
            sessionObj.put(NVP_TOKEN_KEY, decoder.get(NVP_TOKEN_KEY));
            sessionObj.put(REDIRECT_URL_KEY, paypalRedirectURL + decoder.get(NVP_TOKEN_KEY));
            myLog.debug("Session Obj->" + sessionObj.toString());

        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (PayPalException ex) {
            myLog.error("Error on init Payment Transaction->" + ex.getMessage(), ex);
        }
        return sessionObj;
    }

    public UserPaymentInfoVO retrieveUserPaymentInfo(UserVO userVO, JSONObject sessionObj) throws CoreException {
        UserPaymentInfoVO paymentInfoVO = null;
        try {
            String payPalToken = sessionObj.getString(NVP_TOKEN_KEY);
            String strNVPString = prepareCheckExpressDetail(payPalToken);
            String strNVPResponse =
                    (String) payPalService.call(strNVPString);
            myLog.debug("PayPal GetExpressCheckoutDetails Response->" + strNVPResponse);

            NVPDecoder decoder = new NVPDecoder();
            decoder.decode(strNVPResponse);

            paymentInfoVO = new UserPaymentInfoVO();
            paymentInfoVO.setUserVO(userVO);
            paymentInfoVO.setGatewayVO(gwVO);
            paymentInfoVO.setPaymentid(decoder.get("PAYERID"));

            JSONObject configObject = new JSONObject();
            configObject.put("EMAIL", decoder.get("EMAIL"));
            configObject.put("FIRSTNAME", decoder.get("FIRSTNAME"));
            configObject.put("LASTNAME", decoder.get("LASTNAME"));
            configObject.put("COUNTRYCODE", decoder.get("COUNTRYCODE"));

            paymentInfoVO.setAdditionalConfig(configObject.toString());

        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (PayPalException ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return paymentInfoVO;
    }

    @Override
    public PaymentHistoryVO processPaymentTransaction(PaymentType paymentType, PricingPackageVO pricePkgVO,
            UserVO userVO, JSONObject sessionObj) throws CoreException {
        PaymentHistoryVO paymentRecord = null;
        try {
            Double productPrice = 0.00;
            String productCode = null;
            Boolean autoRenew = false;
            Integer noBillCycles = 0;
            String productDesc = null;

            if (PaymentType.NEW_SUBSCRIPTION == paymentType) {
                productPrice = pricePkgVO.getPrice();
                productCode = pricePkgVO.getPkgCode();
                autoRenew = pricePkgVO.isAutoRenew();
                noBillCycles = pricePkgVO.getQuantity();
                productDesc = getPaypalPkgDesc(pricePkgVO);
            }


            NVPDecoder decoder = new NVPDecoder();
            String payPalToken = sessionObj.getString(NVP_TOKEN_KEY);
            String payerID = sessionObj.getString(PAYPAL_PAYER_ID_KEY);
            String nvpRequest = prepareDoExpressCheckOut(payPalToken, payerID, productPrice);
            myLog.debug("Processing payment transaction:" + paymentType.name()
                    + ", productCode:" + productCode
                    + ", userid:" + userVO.getUserid()
                    + ", reqStr->" + nvpRequest);
            String strNVPResponse =
                    (String) payPalService.call(nvpRequest);

            // Error of processing the payment, as customer does not have enough fund
            //
            // TIMESTAMP=2011%2d11%2d14T08%3a59%3a14Z&CORRELATIONID=1542789ec0c29
            //  &ACK=Failure&VERSION=72&BUILD=2256005&L_ERRORCODE0=10417
            // &L_SHORTMESSAGE0=Transaction%20cannot%20complete%2e
            // &L_LONGMESSAGE0=The%20transaction%20cannot%20complete%20successfully%2e%20%20Instruct%20the%20customer%20to%20use%20an%20alternative%20payment%20method%2e
            // &L_SEVERITYCODE0=Error


            myLog.debug("PayPal DoExpressCheck -> payment " + userVO.getUserid()
                    + ",Response->" + strNVPResponse);
            decoder.decode(strNVPResponse);

            if (decoder.get("L_ERRORCODE0") != null) {
                String errMsg = decoder.get("L_LONGMESSAGE0");
                throw new CoreException(ErrorType.PAYMENT_CHARGE_SUBSCRIPTION_ERROR, "Paypal", errMsg);
            }

            Double paymentAmt = decoder.get("PAYMENTINFO_0_AMT") != null
                    ? new Double(decoder.get("PAYMENTINFO_0_AMT")) : 0.00;
            Double paymentFee = decoder.get("PAYMENTINFO_0_FEEAMT") != null
                    ? new Double(decoder.get("PAYMENTINFO_0_FEEAMT")) : 0.00;
            String txnid = decoder.get("PAYMENTINFO_0_TRANSACTIONID");
            String paymentRemark = "PayPal Transaction ID: " + txnid;

            String initialPaymentStatus = decoder.get("PAYMENTINFO_0_PAYMENTSTATUS");
            if (initialPaymentStatus.equalsIgnoreCase("Completed")) {
                // Populate PaymentHistroyVO
                myLog.debug("Payment for user->" + userVO.getUserid() + "successfully charged!");
                paymentRecord = new PaymentHistoryVO();
                paymentRecord.setPaymentGWVO(this.gwVO);
                paymentRecord.setPaymentAmt(paymentAmt);
                paymentRecord.setPaymentDate(new Date());
                paymentRecord.setPaymentFee(paymentFee);
                paymentRecord.setPaymentType(PaymentType.NEW_SUBSCRIPTION.name());
                if (PaymentType.NEW_SUBSCRIPTION.equals(paymentType)) {
                    paymentRecord.setPricingPgkVO(pricePkgVO);
                }
                paymentRecord.setUserVO(userVO);
                paymentRecord.setRemark(paymentRemark);


                // Now Attemp to create subscription profile per PayPal requirement
                // JK 2nd-Apr-2012
                // Only create subscription profile when pricing package is set with 
                // Auto Renew flag
                //
                if (autoRenew) {
                    String paypalEmailAddr = getUserPayPalEmailAddress(userVO);
                    String subscribeRequest =
                            prepareCreateRecurringProfileReq(payPalToken, payerID, paypalEmailAddr,
                            userVO, noBillCycles, productCode, productDesc, productPrice);

                    myLog.debug("Submitting create Subscription profile for user->" + userVO.getUserid()
                            + " , reqStr->" + subscribeRequest);
                    String subscribeResponse =
                            (String) payPalService.call(subscribeRequest);
                    myLog.debug("Paypal createRecurringProfle for " + userVO.getUserid()
                            + ", Response->" + subscribeResponse);
                    NVPDecoder profileDecoder = new NVPDecoder();
                    profileDecoder.decode(subscribeResponse);
                    //Response->
                    // PROFILEID=I%2d6EN6DTWGX6Y1&PROFILESTATUS=ActiveProfile&
                    // TIMESTAMP=2011%2d06%2d15T09%3a33%3a27Z&CORRELATIONID=367059a4e3e04&
                    //ACK=Success&VERSION=65%2e1&BUILD=1863577

                    // Store the value in userSubscription
                    String profileID = profileDecoder.get("PROFILEID");
                    String profileStatus = profileDecoder.get("PROFILESTATUS");

                    // Todo, create a PromotionRecord for user as well
                    if (PaymentType.NEW_SUBSCRIPTION.equals(paymentType)) {
                        paymentHelper.createReplaceSubscriptionRecord(userVO, pricePkgVO, gwVO,
                                pricePkgVO.getPrice(), new Date(), null,
                                profileID, profileStatus, null);
                    }
                }

            } else {
                String failPaymentRemark = "Status=" + initialPaymentStatus
                        + ", PendingReason=" + decoder.get("PAYMENTINFO_0_PENDINGREASON");
                myLog.debug("Fail to charge first time subscription for user->" + userVO.getUserid());
                paymentHelper.insertNewPendingPaymentVO(txnid, this.gwVO.getGwName(),
                        payerID, pricePkgVO, userVO,
                        paymentAmt, paymentFee, failPaymentRemark);
                throw new CoreException(ErrorType.PAYMENT_CHARGE_SUBSCRIPTION_ERROR, this.gwVO.getGwName(), failPaymentRemark);
            }

        } catch (PayPalException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return paymentRecord;

    }

    public void cancelSubscription(UserVO userVO, String cancelNote) {
        try {
            String nvpReqStr = prepareCancelProfileNVPReq(userVO.getUserCurrentSubscription().getProfileid(), cancelNote);
            myLog.debug("Submitting cancel Subscription for user:" + userVO.getUserid()
                    + ", reqStr->" + nvpReqStr);
            String strNVPResponse =
                    (String) payPalService.call(nvpReqStr);
            myLog.debug("PayPal cancel Subscription resp->" + userVO.getUserid()
                    + ",Response->" + strNVPResponse);
        } catch (PayPalException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    private String prepareCancelProfileNVPReq(String profileId, String note) throws PayPalException {
        NVPEncoder encoder = new NVPEncoder();
        encoder.add(NVP_METHOD_KEY, "ManageRecurringPaymentsProfileStatus");
        encoder.add("ACTION", "Cancel");
        encoder.add("PROFILEID", profileId);
        if (note != null) {
            encoder.add("NOTE", note);
        }

        return encoder.encode();
    }

    private String getUserPayPalEmailAddress(UserVO userVO) {
        String emailAddr = null;

        if (userVO.getUserPaymentInfoList() != null && userVO.getUserPaymentInfoList().size() > 0) {
            for (UserPaymentInfoVO paymentInfo : userVO.getUserPaymentInfoList()) {
                if (paymentInfo.getGwName().equals(this.gwVO.getGwName())) {
                    try {
                        emailAddr = new JSONObject(paymentInfo.getAdditionalConfig()).getString("EMAIL");
                    } catch (JSONException ex) {
                        myLog.error(ex.getMessage(), ex);
                    }
                    break;
                }
            }
        }

        return emailAddr;
    }

    private String prepareDoExpressCheckOut(String paypalToken, String payerid,
            Double price) throws PayPalException {
        NVPEncoder encoder = new NVPEncoder();
        encoder.add(NVP_METHOD_KEY, DO_EXPRESS_CHECKOUT_METHOD_NAME);
        encoder.add(NVP_TOKEN_KEY, paypalToken);
        encoder.add(NVP_VERSION_KEY, apiVersion);
        encoder.add(NVP_PAYMENT_AMT_KEY, price.toString());
        encoder.add("PAYERID", payerid);

        return encoder.encode();
    }

    private String prepareCreateRecurringProfileReq(String paypalToken, String payerid, String payerEmail,
            UserVO userVO,
            Integer noBillCycles,
            String productCode,
            String productDesc,
            Double productPrice) throws PayPalException {

        NVPEncoder encoder = new NVPEncoder();
        encoder.add(NVP_METHOD_KEY, CREATE_SUBSCRIPTION_PROFILE_METHOD_NAME);
        encoder.add(NVP_TOKEN_KEY, paypalToken);
        // Unit Invoice id to identified the subscription profile
        // We will use user's activation code + "_____" + pricingPkgCode as unique
        // invoice id
        encoder.add("PROFILEREFERENCE", userVO.getActivationCode() + "_____" + productCode
                + "_____" + new Date().getTime());
//        encoder.add("DESC", getPaypalPkgDesc(pkgVO));
        encoder.add("DESC", productDesc);
//
        Date profileStartDate = DateUtils.addMonths(new Date(), noBillCycles);
        String profileStartDateStr = dateFormat.format(profileStartDate) + "T00:00:00";
        encoder.add("BILLINGPERIOD", "Month");
        
        encoder.add("BILLINGFREQUENCY", noBillCycles.toString());
        encoder.add("PROFILESTARTDATE", profileStartDateStr);
        encoder.add("AMT", productPrice.toString());
        encoder.add("EMAIL", payerEmail);
        encoder.add("L_PAYMENTREQUEST_0_ITEMCATEGORY0", "Digital");
        encoder.add("L_PAYMENTREQUEST_0_NAME0", productCode);
        encoder.add("L_PAYMENTREQUEST_0_AMT0", productPrice.toString());
        encoder.add("CURRENCYCODE", "USD");
        encoder.add("L_PAYMENTREQUEST_0_QTY0", "1");

        return encoder.encode();
    }

    private String prepareCreateRecurringProfileReqForGifting(String paypalToken, String payerid, String payerEmail,
    		GiftPaymentLogVO giftPaymentLogVO,
            Integer noBillCycles,
            String productCode,
            String productDesc,
            Double productPrice) throws PayPalException {

        NVPEncoder encoder = new NVPEncoder();
        encoder.add(NVP_METHOD_KEY, CREATE_SUBSCRIPTION_PROFILE_METHOD_NAME);
        encoder.add(NVP_TOKEN_KEY, paypalToken);
        // Unit Invoice id to identified the gifting profile
        // We will use user's country calling + mobile number + "_____" + pricingPkgCode as unique
        // invoice id
        encoder.add("PROFILEREFERENCE", "GIFT_" + giftPaymentLogVO.getMobileCountry().getIso()
        		+ "-" + giftPaymentLogVO.getMobileNumber()
        		+ "_____" + productCode
                + "_____" + new Date().getTime());
//        encoder.add("DESC", getPaypalPkgDesc(pkgVO));
        encoder.add("DESC", productDesc);
//
        Date profileStartDate = DateUtils.addMonths(new Date(), noBillCycles);
        String profileStartDateStr = dateFormat.format(profileStartDate) + "T00:00:00";
        encoder.add("BILLINGPERIOD", "Month");
        
        encoder.add("BILLINGFREQUENCY", noBillCycles.toString());
        encoder.add("PROFILESTARTDATE", profileStartDateStr);
        encoder.add("AMT", productPrice.toString());
        encoder.add("EMAIL", payerEmail);
        encoder.add("L_PAYMENTREQUEST_0_ITEMCATEGORY0", "Digital");
        encoder.add("L_PAYMENTREQUEST_0_NAME0", productCode);
        encoder.add("L_PAYMENTREQUEST_0_AMT0", productPrice.toString());
        encoder.add("CURRENCYCODE", "USD");
        encoder.add("L_PAYMENTREQUEST_0_QTY0", "1");

        return encoder.encode();
    }
    
    private String prepareCheckExpressDetail(String paypalToken) throws PayPalException {
        NVPEncoder encoder = new NVPEncoder();
        encoder.add(NVP_METHOD_KEY, GET_EXPRESS_CHECKOUT_DETAIL_NAME);
        encoder.add(NVP_TOKEN_KEY, paypalToken);
        encoder.add(NVP_PAYMENT_ACTION_KEY, "Sale");
        return encoder.encode();
    }

    private String prepareInitPaymentReqString(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String productCode, Double price,
            UserVO userVO) throws PayPalException {
        NVPEncoder encoder = new NVPEncoder();
        encoder.add(NVP_METHOD_KEY, SET_EXPRESS_CHECKOUT_NAME);
        encoder.add(NVP_VERSION_KEY, apiVersion);
        encoder.add(NVP_PAYMENT_ACTION_KEY, "Sale");
        if (autoRenew) {
            encoder.add("L_BILLINGTYPE0", "RecurringPayments");
            encoder.add("L_BILLINGAGREEMENTDESCRIPTION0", pkgDesc);
        } else {
            encoder.add("L_PAYMENTREQUEST_0_NAME0", pkgDesc);
            encoder.add("L_PAYMENTREQUEST_0_DESC0", pkgDesc);
            encoder.add("L_PAYMENTREQUEST_0_AMT0", price.toString());
        }
        encoder.add("ALLOWNOTE", "0");
        encoder.add("HDRIMG", "https://secq.me/mainportal/images/paypalpanel.png");
        encoder.add("HDRBACKCOLOR", "021627");
        encoder.add("BRANDNAME", "SECQME SDN BHD");
        encoder.add("PAYMENTREQUEST_0_INVNUM", userVO.getActivationCode()
                + "_____" + productCode + "_____" + new Date().getTime());
        encoder.add(NVP_PAYMENT_AMT_KEY, price.toString());

        // append the userToken and pricingPkg pgkcode in 
        // both return and cancel URL
        String authorizedURL = returnURL + "?" + USER_TOKEN_KEY + "="
                + userVO.getActivationCode() + "&" + PAYMENT_TYPE_KEY + "=" + paymentType.name() + "&";
        String paymentCancelURL = cancelURL + "?usertoken=" + userVO.getActivationCode() + "&"
                + PAYMENT_TYPE_KEY + "=" + paymentType.name() + "&";

        if (PaymentType.NEW_SUBSCRIPTION == paymentType) {
            authorizedURL = authorizedURL + PRICE_PKG_CODE_KEY + "=";
            paymentCancelURL = paymentCancelURL + PRICE_PKG_CODE_KEY + "=";
        } else {
            authorizedURL = authorizedURL + PROMOTION_CODE_KEY + "=";
            paymentCancelURL = paymentCancelURL + PROMOTION_CODE_KEY + "=";
        }
        authorizedURL = authorizedURL + productCode;
        paymentCancelURL = paymentCancelURL + productCode;

        encoder.add(NVP_RETURN_URL_KEY, authorizedURL);
        encoder.add(NVP_CANCEL_URL_KEY, paymentCancelURL);
        encoder.add("REQCONFIRMSHIPPING", "0");
        encoder.add("NOSHIPPING", "1");
        return encoder.encode();
    }

    public JSONObject initPaymentTransactionForGifting(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String productCode, Double price, GiftPaymentLogVO giftPaymentLogVO) throws CoreException {
    	JSONObject sessionObj = new JSONObject();
        try {
            myLog.debug("Initialize gift payment transaction for:" + giftPaymentLogVO.getRecipientMobileCountry().getCallingCode()
            		+ giftPaymentLogVO.getRecipientMobileNumber() 
                    + ", paymentType:" + paymentType.name()
                    + ", pkg:" + productCode
                    + ", price->" + price
                    + ", autoRenew->" + giftPaymentLogVO.isAutoRenew());
            NVPDecoder decoder = new NVPDecoder();
            String strNVPString = prepareInitPaymentReqStringForGifting(paymentType, autoRenew, pkgDesc, productCode, price, giftPaymentLogVO);
            myLog.debug("Encoded request->" + strNVPString);

            String strNVPResponse =
                    (String) payPalService.call(strNVPString);
            myLog.debug("PayPal setExpressCheckout Response->" + strNVPResponse);
            decoder.decode(strNVPResponse);
            sessionObj.put(NVP_TOKEN_KEY, decoder.get(NVP_TOKEN_KEY));
            sessionObj.put(REDIRECT_URL_KEY, paypalRedirectURL + decoder.get(NVP_TOKEN_KEY));
            myLog.debug("Session Obj->" + sessionObj.toString());

        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (PayPalException ex) {
            myLog.error("Error on init Payment Transaction->" + ex.getMessage(), ex);
        }
        return sessionObj;
    }
    
    private String prepareInitPaymentReqStringForGifting(PaymentType paymentType, Boolean autoRenew,
            String pkgDesc, String productCode, Double price,
            GiftPaymentLogVO giftPaymentLogVO) throws PayPalException {
    	NVPEncoder encoder = new NVPEncoder();
        encoder.add(NVP_METHOD_KEY, SET_EXPRESS_CHECKOUT_NAME);
        encoder.add(NVP_VERSION_KEY, apiVersion);
        encoder.add(NVP_PAYMENT_ACTION_KEY, "Sale");
//        if (autoRenew) {
        if(giftPaymentLogVO.isAutoRenew()) {
            encoder.add("L_BILLINGTYPE0", "RecurringPayments");
            encoder.add("L_BILLINGAGREEMENTDESCRIPTION0", pkgDesc);
        } else {
            encoder.add("L_PAYMENTREQUEST_0_NAME0", pkgDesc);
            encoder.add("L_PAYMENTREQUEST_0_DESC0", pkgDesc);
            encoder.add("L_PAYMENTREQUEST_0_AMT0", price.toString());
        }
        encoder.add("ALLOWNOTE", "0");
        encoder.add("HDRIMG", "https://secq.me/mainportal/images/paypalpanel.png");
        encoder.add("HDRBACKCOLOR", "021627");
        encoder.add("BRANDNAME", "SECQME SDN BHD");
        encoder.add("PAYMENTREQUEST_0_INVNUM", "GIFT_" 
       		 + giftPaymentLogVO.getRecipientMobileCountry().getCallingCode() + "-"
       		 + giftPaymentLogVO.getRecipientMobileNumber()
                + "_____" + productCode + "_____" + new Date().getTime());
        encoder.add(NVP_PAYMENT_AMT_KEY, price.toString());

        // append the recipient and pricingPkg pgkcode in 
        // both return and cancel URL
        String authorizedURL = returnURL + "?" 
       		 + GIFT_NAME_KEY + "=" + giftPaymentLogVO.getName() 
       		 + "&" + GIFT_MOBILE_COUNTRY_KEY + "=" + giftPaymentLogVO.getMobileCountry().getIso()
       		 + "&" + GIFT_MOBILE_NUMBER_KEY + "=" + giftPaymentLogVO.getMobileNumber()
       		 + "&" + GIFT_RECIPIENT_NAME_KEY+ "=" + giftPaymentLogVO.getRecipientName()
       		 + "&" + GIFT_EMAIL + "=" + giftPaymentLogVO.getEmail()
       		 + "&" + GIFT_MESSAGE + "=" + giftPaymentLogVO.getMessage()
       		 + "&" + GIFT_RECIPIENT_MOBILE_COUNTRY_KEY + "=" + giftPaymentLogVO.getRecipientMobileCountry().getIso()
       		 + "&" + GIFT_RECIPIENT_MOBILE_NUMBER_KEY + "=" + giftPaymentLogVO.getRecipientMobileNumber()
       		 + "&" + GIFT_RECIPIENT_EMAIL_KEY + "=" + giftPaymentLogVO.getRecipientEmail()
       		 + "&" + PAYMENT_TYPE_KEY + "=" + paymentType.name() 
       		 + "&" + GIFT_SUBSCRIBE + "=" + giftPaymentLogVO.isAutoRenew() + "&";
        String paymentCancelURL = cancelURL + "?"
       		 + GIFT_NAME_KEY + "=" + giftPaymentLogVO.getName() 
       		 + "&" + GIFT_MOBILE_COUNTRY_KEY + "=" + giftPaymentLogVO.getMobileCountry().getIso()
       		 + "&" + GIFT_MOBILE_NUMBER_KEY + "=" + giftPaymentLogVO.getMobileNumber()
       		 + "&" + GIFT_EMAIL + "=" + giftPaymentLogVO.getEmail()
       		 + "&" + GIFT_MESSAGE + "=" + giftPaymentLogVO.getMessage()
       		 + "&" + GIFT_RECIPIENT_NAME_KEY+ "=" + giftPaymentLogVO.getRecipientName()
       		 + "&" + GIFT_RECIPIENT_MOBILE_COUNTRY_KEY + "=" + giftPaymentLogVO.getRecipientMobileCountry().getIso()
       		 + "&" + GIFT_RECIPIENT_MOBILE_NUMBER_KEY + "=" + giftPaymentLogVO.getRecipientMobileNumber()
       		 + "&" + GIFT_RECIPIENT_EMAIL_KEY + "=" + giftPaymentLogVO.getRecipientEmail()
       		 + "&" + PAYMENT_TYPE_KEY + "=" + paymentType.name() 
       		 + "&" + GIFT_SUBSCRIBE + "=" + giftPaymentLogVO.isAutoRenew() + "&";

        if (PaymentType.NEW_SUBSCRIPTION == paymentType) {
            authorizedURL = authorizedURL + PRICE_PKG_CODE_KEY + "=";
            paymentCancelURL = paymentCancelURL + PRICE_PKG_CODE_KEY + "=";
        } else {
            authorizedURL = authorizedURL + PROMOTION_CODE_KEY + "=";
            paymentCancelURL = paymentCancelURL + PROMOTION_CODE_KEY + "=";
        }
        authorizedURL = authorizedURL + productCode;
        paymentCancelURL = paymentCancelURL + productCode;

        encoder.add(NVP_RETURN_URL_KEY, authorizedURL);
        encoder.add(NVP_CANCEL_URL_KEY, paymentCancelURL);
        encoder.add("REQCONFIRMSHIPPING", "0");
        encoder.add("NOSHIPPING", "1");
        return encoder.encode();
    }
    
    private String getPaypalPkgDesc(PricingPackageVO pkgVO) {
        return pkgVO.getPkgDesc() + ", (" + pkgVO.getCurrencyCode() + ") "
                + pkgVO.getPrice();
    }

    private NVPCallerServices initPayPalService() throws PayPalException {
        apiProfile = ProfileFactory.createSignatureAPIProfile();
        apiProfile.setAPIUsername(apiUserName);
        apiProfile.setAPIPassword(apiUserPassword);
        apiProfile.setSignature(apiSignature);
        apiProfile.setEnvironment(environment);
        apiProfile.setSubject("");

        payPalService = new NVPCallerServices();
        payPalService.setAPIProfile(apiProfile);

        return payPalService;
    }

    public JSONObject getPaymentGWConfig() {
        return gwVO.getConfig();
    }

    public PaymentHistoryVO handleGatewayNotification(UserVO userVO, PricingPackageVO pricingPkgVO,
            JSONObject notificationObj) throws CoreException {
        // Case 1, Instant payment with pending status..
        // payment_fee=0.47, first_name=Test, mc_fee=0.47, mc_gross=6.00, payment_type=instant, mc_currency=USD
        // txn_type=express_checkout, verify_sign=AvVRUoA6hU-DbDYMBCG8ZOC.FTZiAaHOIYBCYOUCVPKJG9COnNPpNJ4F
        // however needs to process the payment_status = pending.

        // Case 2, Instant Payment with completed payment status
        // Similar to, payment_type=instant  means subscription first month payment
        // payment_status=Completed

        // JK March 27-2012
        // Needs to worry about refund case?
        // mc_gross=-4.99&period_type= Regular&outstanding_balance=0.00&
        // next_payment_date=03:00:00 Apr 19, 2012 PDT&protection_eligibility=Ineligible&
        // payment_cycle=Monthly&payer_id=A2RQL89Q7FLGA&payment_date=22:06:36 Mar 24, 2012 PDT&
        // payment_status=Refunded&product_name=Personal PLATINUM Package, unlimited credits for event 
        // Registration, 20 international SMS credits, unlimited emergency contacts&
        // charset=windows-1252&
        // rp_invoice_id=0416f2af-2a58-401b-8a29-f19c90f2f084_____PLAT_DF_1M_____1329729450337&
        // recurring_payment_id=I-KPCDCCC1USXP&first_name=Chen Shiang&mc_fee=-0.47&
        //notify_version=3.4&amount_per_cycle=4.99&reason_code=refund&currency_code=USD&
        //business=james.khoo@secq.me&verify_sign=AWsNo0aIrfrRWeUVJaGqD147KrlrAcsROz2.gkZU3CdDtd6HVv.CXLqy&
        //payer_email=khoo.james@gmail.com&parent_txn_id=2UJ81070NK0362545&initial_payment_amount=0.00&
        // profile_status=Active&amount=4.99&txn_id=0XH507977B873612V&payment_type=instant&last_name=Khoo&
        // receiver_email=james.khoo@secq.me&payment_fee=-0.47&receiver_id=FVLGXLB5AUCUQ&mc_currency=USD&
        //residence_country=MY&transaction_subject=Personal PLATINUM Package, unlimited credits for event Registration, 20 international SMS credits, unlimited emergency contacts&
        // payment_gross=-4.99&shipping=0.00&product_type=1&time_created=01:17:30 Feb 20, 2012 PST&
        // ipn_track_id=2764a904e2dfe

        PaymentHistoryVO paymentHistoryVO = null;

        try {
            String txnType = notificationObj.getString("txn_type");
            if (txnType != null) {
                if (txnType.startsWith("recurring_payment_profile")) {
                    paymentHistoryVO = handleSubscriptionProfileMgmt(userVO, pricingPkgVO, notificationObj);
                } else if (txnType.equals("recurring_payment")) {
                    paymentHistoryVO = handleSubscriptionPayment(userVO, pricingPkgVO, notificationObj);
                }
            }
        } catch (ParseException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return paymentHistoryVO;
    }

    private PaymentHistoryVO handleSubscriptionPayment(UserVO userVO, PricingPackageVO pricingPkgVO,
            JSONObject notificationObj) throws CoreException, JSONException, ParseException {
        PaymentHistoryVO paymentRecord = new PaymentHistoryVO();
        // Update user's Subscription on Next Payment Date;
        Date nextPaymentDate = null;
        if (notificationObj.has("next_payment_date")
                && !notificationObj.getString("next_payment_date").equals("N/A")) {
            nextPaymentDate = paypalDateFormat.parse(notificationObj.getString("next_payment_date"));
        }

        paymentHelper.createReplaceSubscriptionRecord(userVO, pricingPkgVO, gwVO,
                null, null, nextPaymentDate,
                null, null, null);

        paymentRecord.setPaymentGWVO(this.gwVO);
        paymentRecord.setPaymentAmt(new Double(notificationObj.getString("payment_gross")));
        paymentRecord.setPaymentFee(new Double(notificationObj.getString("payment_fee")));
        paymentRecord.setPaymentDate(paypalDateFormat.parse(notificationObj.getString("payment_date")));
        paymentRecord.setPaymentType(PaymentType.SUBSCRIPTION_RENEW.name());
        paymentRecord.setPricingPgkVO(pricingPkgVO);
        paymentRecord.setUserVO(userVO);

        // Forming Payment Remark;
        paymentRecord.setRemark("PayPal Transaction ID: " + notificationObj.getString("txn_id"));
        return paymentRecord;
    }

    private PaymentHistoryVO handleSubscriptionProfileMgmt(UserVO userVO, PricingPackageVO pricingPkgVO,
            JSONObject notificationObj) throws CoreException, JSONException, ParseException {
        PaymentHistoryVO paymentRecord = null;
        // updating user's payment Profile for user;
        myLog.debug("updating user's subcription profile for user->" + userVO.getUserid());
        String profileStatus = notificationObj.getString("profile_status");
        if (profileStatus.equalsIgnoreCase("Cancelled")) {
            myLog.debug("User " + userVO.getUserid() + " has cancel the subscription");
            userVO.setUserCurrentSubscription(null);
            paymentHelper.updateUserSubscriptionInfo(userVO);
            paymentRecord = new PaymentHistoryVO();
            paymentRecord.setPaymentGWVO(this.gwVO);
            paymentRecord.setPaymentAmt(new Double(0.00));
            paymentRecord.setPaymentDate(paypalDateFormat.parse(notificationObj.getString("time_created")));
            paymentRecord.setPaymentType(PaymentType.CANCEL_SUBSCRIPTION.name());
            paymentRecord.setPricingPgkVO(pricingPkgVO);
            paymentRecord.setUserVO(userVO);
        } else {
            Date nextPaymentDate = null;
            if (notificationObj.has("next_payment_date")
                    && !notificationObj.getString("next_payment_date").equals("N/A")) {
                nextPaymentDate = paypalDateFormat.parse(notificationObj.getString("next_payment_date"));
            }

            Date effectiveDate = null;
            if (notificationObj.has("time_created")
                    && !notificationObj.getString("time_created").equals("N/A")) {
                effectiveDate = paypalDateFormat.parse(notificationObj.getString("time_created"));
            }

            String profileID = null;
            if (notificationObj.has("recurring_payment_id")) {
                profileID = notificationObj.getString("recurring_payment_id");
            }

            paymentHelper.createReplaceSubscriptionRecord(userVO, pricingPkgVO, gwVO,
                    null, effectiveDate, nextPaymentDate,
                    profileID, profileStatus, null);
        }

        return paymentRecord;
    }

    public PaymentGWVO getPaymentGWVO() {
        return gwVO;
    }
    
    public PaymentHistoryVO processPaymentTransactionForGifting(PaymentType paymentType, PricingPackageVO pricePkgVO,
    		JSONObject sessionObj, GiftPaymentLogVO giftPaymentLogVO) throws CoreException {
    	PaymentHistoryVO paymentRecord = null;
        try {
            Double productPrice = 0.00;
            String productCode = null;
            Boolean autoRenew = false;
            Integer noBillCycles = 0;
            String productDesc = null;

            if (PaymentType.NEW_SUBSCRIPTION == paymentType) {
                productPrice = pricePkgVO.getPrice();
                productCode = pricePkgVO.getPkgCode();
                autoRenew = pricePkgVO.isAutoRenew();
                noBillCycles = pricePkgVO.getQuantity();
                productDesc = getPaypalPkgDesc(pricePkgVO);
            }


            NVPDecoder decoder = new NVPDecoder();
            String payPalToken = sessionObj.getString(NVP_TOKEN_KEY);
            String payerID = sessionObj.getString(PAYPAL_PAYER_ID_KEY);
            String nvpRequest = prepareDoExpressCheckOut(payPalToken, payerID, productPrice);
            myLog.debug("Processing payment transaction:" + paymentType.name()
                    + ", productCode:" + productCode
                    + ", recipient country:" + giftPaymentLogVO.getRecipientMobileCountry().getIso()
                    + ", recipient mobile:" + giftPaymentLogVO.getRecipientMobileNumber()
                    + ", reqStr->" + nvpRequest);
            String strNVPResponse =
                    (String) payPalService.call(nvpRequest);

            // Error of processing the payment, as customer does not have enough fund
            //
            // TIMESTAMP=2011%2d11%2d14T08%3a59%3a14Z&CORRELATIONID=1542789ec0c29
            //  &ACK=Failure&VERSION=72&BUILD=2256005&L_ERRORCODE0=10417
            // &L_SHORTMESSAGE0=Transaction%20cannot%20complete%2e
            // &L_LONGMESSAGE0=The%20transaction%20cannot%20complete%20successfully%2e%20%20Instruct%20the%20customer%20to%20use%20an%20alternative%20payment%20method%2e
            // &L_SEVERITYCODE0=Error


            myLog.debug("PayPal DoExpressCheck -> payment " + giftPaymentLogVO.getRecipientName()
                    + ",Response->" + strNVPResponse);
            decoder.decode(strNVPResponse);

            if (decoder.get("L_ERRORCODE0") != null) {
                String errMsg = decoder.get("L_LONGMESSAGE0");
                throw new CoreException(ErrorType.PAYMENT_CHARGE_SUBSCRIPTION_ERROR, "Paypal", errMsg);
            }

            Double paymentAmt = decoder.get("PAYMENTINFO_0_AMT") != null
                    ? new Double(decoder.get("PAYMENTINFO_0_AMT")) : 0.00;
            Double paymentFee = decoder.get("PAYMENTINFO_0_FEEAMT") != null
                    ? new Double(decoder.get("PAYMENTINFO_0_FEEAMT")) : 0.00;
            String txnid = decoder.get("PAYMENTINFO_0_TRANSACTIONID");
            String paymentRemark = "PayPal Transaction ID: " + txnid;

            String initialPaymentStatus = decoder.get("PAYMENTINFO_0_PAYMENTSTATUS");
            if (initialPaymentStatus.equalsIgnoreCase("Completed")) {
                // Populate PaymentHistroyVO
                myLog.debug("Payment for mobile number->"
                		+ giftPaymentLogVO.getRecipientMobileCountry().getCallingCode()
                		+ giftPaymentLogVO.getRecipientMobileNumber()
                		+ "successfully charged!");
                paymentRecord = new PaymentHistoryVO();
                paymentRecord.setPaymentGWVO(this.gwVO);
                paymentRecord.setPaymentAmt(paymentAmt);
                paymentRecord.setPaymentDate(new Date());
                paymentRecord.setPaymentFee(paymentFee);
                paymentRecord.setPaymentType(PaymentType.NEW_SUBSCRIPTION.name());
                if (PaymentType.NEW_SUBSCRIPTION.equals(paymentType)) {
                    paymentRecord.setPricingPgkVO(pricePkgVO);
                }
                paymentRecord.setRemark(paymentRemark);
                paymentRecord.setGiftedUserid(giftPaymentLogVO.getRecipientMobileCountry().getCallingCode()
                		+ "-" + giftPaymentLogVO.getRecipientMobileNumber());

                // Now Attemp to create subscription profile per PayPal requirement
                // JK 2nd-Apr-2012
                // Only create subscription profile when pricing package is set with 
                // Auto Renew flag
                //
                if (autoRenew) {
                	//TODO use mobilenumber as email address?
                    String paypalEmailAddr = giftPaymentLogVO.getRecipientMobileCountry().getCallingCode() + "-"
                    		+ giftPaymentLogVO.getRecipientMobileNumber();
                    String subscribeRequest =
                    		prepareCreateRecurringProfileReqForGifting(payPalToken, payerID, paypalEmailAddr,
                    				giftPaymentLogVO, noBillCycles, productCode, productDesc, productPrice);

                    myLog.debug("Submitting create Gifting Subscription profile for user->" 
                    		+ giftPaymentLogVO.getRecipientMobileCountry().getCallingCode() 
                    		+ giftPaymentLogVO.getMobileNumber()
                            + " , reqStr->" + subscribeRequest);
                    String subscribeResponse =
                            (String) payPalService.call(subscribeRequest);
                    myLog.debug("Paypal Gifting createRecurringProfle for " 
                    		+ giftPaymentLogVO.getRecipientMobileCountry().getCallingCode() 
                    		+ giftPaymentLogVO.getMobileNumber()
                            + ", Response->" + subscribeResponse);
                    NVPDecoder profileDecoder = new NVPDecoder();
                    profileDecoder.decode(subscribeResponse);
                    //Response->
                    // PROFILEID=I%2d6EN6DTWGX6Y1&PROFILESTATUS=ActiveProfile&
                    // TIMESTAMP=2011%2d06%2d15T09%3a33%3a27Z&CORRELATIONID=367059a4e3e04&
                    //ACK=Success&VERSION=65%2e1&BUILD=1863577

                    // Store the value in userSubscription
                    String profileID = profileDecoder.get("PROFILEID");
                    String profileStatus = profileDecoder.get("PROFILESTATUS");

                    // TODO - create sub after user created acc
//                    if (PaymentType.NEW_SUBSCRIPTION.equals(paymentType)) {
//                        paymentHelper.createReplaceSubscriptionRecord(userVO, pricePkgVO, gwVO,
//                                pricePkgVO.getPrice(), new Date(), null,
//                                profileID, profileStatus, null);
//                    }
                }

            } else {
                String failPaymentRemark = "Status=" + initialPaymentStatus
                        + ", PendingReason=" + decoder.get("PAYMENTINFO_0_PENDINGREASON");
                myLog.debug("Fail to charge gifting for recipient->" + giftPaymentLogVO.getRecipientMobileCountry().getCallingCode()
                		+ giftPaymentLogVO.getRecipientMobileNumber());
                paymentHelper.insertNewPendingPaymentVO(txnid, this.gwVO.getGwName(),
                        payerID, pricePkgVO, null,
                        paymentAmt, paymentFee, failPaymentRemark);
                throw new CoreException(ErrorType.PAYMENT_CHARGE_SUBSCRIPTION_ERROR, this.gwVO.getGwName(), failPaymentRemark);
            }

        } catch (PayPalException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return paymentRecord;
    }
    
    //from gifting notification
    public PaymentHistoryVO handleGatewayNotification(String giftedUserid, PricingPackageVO pricingPkgVO, 
    		JSONObject notificationObj) throws CoreException {
    	
    	PaymentHistoryVO paymentRecord = null;
    	try{
	    	String txnType = notificationObj.getString("txn_type");
	        if (txnType != null) {
	            if (txnType.startsWith("recurring_payment_profile")) {
	            	String profileStatus = notificationObj.getString("profile_status");
	                if (profileStatus.equalsIgnoreCase("Cancelled")) {
	                	paymentRecord = new PaymentHistoryVO();
		            	paymentRecord.setPaymentGWVO(this.gwVO);
		                paymentRecord.setPaymentAmt(new Double(0.00));
		                paymentRecord.setPaymentDate(paypalDateFormat.parse(notificationObj.getString("time_created")));
		                paymentRecord.setPaymentType(PaymentType.CANCEL_SUBSCRIPTION.name());
		                paymentRecord.setPricingPgkVO(pricingPkgVO);
		                paymentRecord.setGiftedUserid(giftedUserid);
	                } else {
	                	//do nothing, no profile to be updated as user does not have account
	                }
	            	
	            } else if (txnType.equals("recurring_payment")) {
	            	paymentRecord = new PaymentHistoryVO();
	            	paymentRecord.setPaymentGWVO(this.gwVO);
	                paymentRecord.setPaymentAmt(new Double(notificationObj.getString("payment_gross")));
	                paymentRecord.setPaymentFee(new Double(notificationObj.getString("payment_fee")));
	                paymentRecord.setPaymentDate(paypalDateFormat.parse(notificationObj.getString("payment_date")));
	                paymentRecord.setPaymentType(PaymentType.SUBSCRIPTION_RENEW.name());
	                paymentRecord.setPricingPgkVO(pricingPkgVO);
	                // Forming Payment Remark;
	                paymentRecord.setRemark("PayPal Transaction ID: " + notificationObj.getString("txn_id"));
	                paymentRecord.setGiftedUserid(giftedUserid);
	            }
	            
	        }
		} catch (ParseException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return paymentRecord;
    }
}

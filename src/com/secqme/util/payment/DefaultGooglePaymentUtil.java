package com.secqme.util.payment;

import com.secqme.CoreException;
import com.secqme.domain.dao.GooglePaymentLogDAO;
import com.secqme.domain.dao.PaymentHistoryDAO;
import com.secqme.domain.dao.PricingPackageDAO;
import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.GooglePaymentLogVO;
import com.secqme.manager.billing.BillingManager;
import com.secqme.manager.payment.PaymentManager;
import com.secqme.util.notification.NotificationEngine;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * User: James Khoo
 * Date: 4/15/14
 * Time: 2:00 PM
 */
public class DefaultGooglePaymentUtil extends BasePaymentUtil implements PaymentUtil {

    public static final String RECEIPT_NO_VERIFY_STATUS = "NO_VERIFY";
    public static final String RECEPIT_VERIFY_STATUS = "RECEIPT_VERIFY";
    public static final String PAYMENT_RENEW_STATUS = "RENEW";

    private static final String GOOGLE_GW_NAME = "GOOGLE";
  	private String refreshTokenUrl = null;
  	private String subscriptionUrl = null;
  	private String clientId = null;
  	private String secret = null;
  	private String refreshToken = null;
  	
    private static Logger myLog = Logger.getLogger(DefaultGooglePaymentUtil.class);
    private GooglePaymentLogDAO googlePaymentLogDAO = null;
    private RestUtil restUtil = null;
    private PaymentManager paymentManager = null;
    private PaymentGateway googlePaymentGW = null;

    public DefaultGooglePaymentUtil(
                                    PaymentHistoryDAO paymntHistDAO,
                                    RestUtil restUtil,
                                    PricingPackageDAO pricePkgDAO,
                                    UserDAO userDAO,
                                    BillingManager billingManager,
                                    PaymentManager paymentMgr,
                                    NotificationEngine notificationEngine,
                                    GooglePaymentLogDAO paymentLogDAO,
                                    String refreshTokenUrl, 
                                    String subscriptionUrl,
                                    String clientId,
                                    String secret,
                                    String refreshToken) {
        super(paymntHistDAO, pricePkgDAO, userDAO, billingManager, notificationEngine);
        this.googlePaymentLogDAO = paymentLogDAO;
        this.paymentManager = paymentMgr;
        this.restUtil = restUtil;
        this.refreshTokenUrl = refreshTokenUrl;
        this.subscriptionUrl = subscriptionUrl;
        this.clientId = clientId;
        this.secret = secret;
        this.refreshToken = refreshToken;
        init();
    }


    public void processPayment(String receiptData, UserVO userVO) throws CoreException {
    	
        try {
        	JSONObject receiptObject = new JSONObject(receiptData);
        	String result = verifyGooglePayment(receiptObject.getString("purchaseToken"), receiptObject.getString("productId"));
        	GooglePaymentLogVO paymentLogVO = null;
        	if(result == null) { //verification failed, still insert to log, might be connection fail
        		paymentLogVO = insertGooglePaymentLog(userVO, receiptData, RECEIPT_NO_VERIFY_STATUS, null);
        	} else {
        		JSONObject resultObject = new JSONObject(result);
        		if(resultObject.has("validUntilTimestampMsec")) { //successful verification
        			Date expiryDate = new Date(resultObject.getLong("validUntilTimestampMsec"));
        			paymentLogVO = insertGooglePaymentLog(userVO, receiptData, RECEPIT_VERIFY_STATUS, expiryDate);
        		} else { //verification failed 
        			paymentLogVO = insertGooglePaymentLog(userVO, receiptData, RECEIPT_NO_VERIFY_STATUS, null);
        		}
        		
        		
        	}
            
            activateUserPremiumPackage(paymentLogVO.getProductID(), userVO, null, googlePaymentGW, null);
        } catch(JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }


    }

    public void processPaymentWithVerifiedReceipt(JSONObject verifiedReceipt, UserVO userVO) throws CoreException {}

    private void init() {
        googlePaymentGW = paymentManager.getPaymentGW(GOOGLE_GW_NAME);
        myLog.debug("Google Payment Gateway initialized!");
    }

    private GooglePaymentLogVO insertGooglePaymentLog(UserVO userVO, String receiptData, String receiptStatus,
    		Date expiryDate) throws JSONException {
         /*
         "googleInAppReceipt":
          {
             "orderId":"12999763169054705758.1371079406387615",
             "packageName":"com.example.app",
             "productId":"ar_pp_1m",
             "purchaseTime":1345678900000,
             "purchaseState":0,
             "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
             "purchaseToken":"rojeslcdyyiapnqcynkjyyjh"
          }
         */
        JSONObject googleReceiptObj = new JSONObject(receiptData);
        String productID = googleReceiptObj.getString("productId").toUpperCase();
        Date purchaseTime = new Date(googleReceiptObj.getLong("purchaseTime"));
        String purchaseToken = googleReceiptObj.getString("purchaseToken");
        GooglePaymentLogVO paymentLogVO = new GooglePaymentLogVO();
        paymentLogVO.setUserVO(userVO);
        paymentLogVO.setReceiptDate(purchaseTime);
        paymentLogVO.setProductID(productID);
        paymentLogVO.setPurchaseToken(purchaseToken);
        paymentLogVO.setPurchaseReceipt(receiptData);
        paymentLogVO.setStatus(receiptStatus);
        paymentLogVO.setExpiryDate(expiryDate);
        googlePaymentLogDAO.create(paymentLogVO);

        return paymentLogVO;
    }
    
    public String verifyGooglePayment(String purchaseToken, String productId) {
    	/* token json result and google receipt verification result
    	 * {
			  "access_token" : "ya29.1.AADtN_Vn8BrDb5ue0ShLzKiuTs3EYZM4hw9x4xTG5bxg8TgGKWI1IJzc2Eyh8qMjaXn-5g",
			  "token_type" : "Bearer",
			  "expires_in" : 3600,
			  "refresh_token" : "1/qPmp5Prh-hawjGodnv1Al-glatHzTYwrn3woChrk_Ks"
			}
			
			{
			 "kind": "androidpublisher#subscriptionPurchase",
			 "initiationTimestampMsec": "1397798072624",
			 "validUntilTimestampMsec": "1400433272624",
			 "autoRenewing": false
			}
    	 */
    	HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("grant_type", "refresh_token");
        parameters.put("refresh_token", refreshToken);
        parameters.put("client_id", clientId);
        parameters.put("client_secret", secret);
        String verifyResult = null;
        try {
        	String tokenResult = restUtil.executePost(refreshTokenUrl, parameters, null);
			myLog.debug("google token " + tokenResult);
			JSONObject jsonObject = new JSONObject(tokenResult);
			if(jsonObject.has("access_token")) {
				String verifySubscription = subscriptionUrl
						.replaceAll("%s", productId) + purchaseToken +
						"?access_token=" + jsonObject.getString("access_token");
				myLog.debug("google url " + verifySubscription);
				verifyResult = restUtil.executeGet(verifySubscription, null);
				myLog.debug("google result " + verifyResult);
				
			}
		} catch (RestExecException ex) {
            myLog.error(ex.getMessage(), ex);
		} catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
		}
        return verifyResult;
    }
}

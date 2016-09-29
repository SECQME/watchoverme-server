package com.secqme.util.payment;

import com.secqme.CoreException;
import com.secqme.domain.dao.ITunePaymentLogDAO;
import com.secqme.domain.dao.PaymentHistoryDAO;
import com.secqme.domain.dao.PricingPackageDAO;
import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.ItunePaymentLogVO;
import com.secqme.manager.billing.BillingManager;
import com.secqme.manager.payment.PaymentManager;
import com.secqme.util.SystemProperties;
import com.secqme.util.notification.NotificationEngine;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * @author coolboykl
 */
public class DefaultApplePaymentUtil extends BasePaymentUtil implements PaymentUtil {

    private static final Logger myLog = Logger.getLogger(DefaultApplePaymentUtil.class);
    private static final String APPLE_RECEIPT_DATA_KEY = "receipt-data";
    private static final String APPLE_SHARE_SECRET_KEY = "password";
    private static final String APPLE_RECEIPT_KEY = "receipt";
    private static final String APPLE_LATEST_RECEIPT_KEY = "latest_receipt";
    private static final String APPLE_LATEST_RECEIPT_INFO_KEY = "latest_receipt_info";
    private static final String APPLE_STATUS_KEY = "status";
    private static final String APPLE_ORG_TRANSACTION_ID_KEY = "original_transaction_id";
    private static final String APPLE_PRODUCT_ID_KEY = "product_id";
    private static final String APPLE_ORG_PURCHASE_DATE_KEY = "original_purchase_date";
    private static final String APPLE_EXPIRE_DATE_KEY = "expires_date";
    private static final String APPLE_GW_NAME = "APPLE";
    private static String APPLE_SHARE_SECRET = null;
    //"2011-09-24 10:31:08 Etc/GMT"
    //2011-09-27 06:50:35 Etc/GMT
    private static SimpleDateFormat APPLE_DATE_FORMAT = null;
    private String inAppRESTURL = null;
    private String inAppRESTTestURL = null;
    private ITunePaymentLogDAO iTunePymntLogDAO = null;
    private RestUtil restUtil = null;
    private PaymentGateway applePaymentGW = null;
    private PaymentManager paymentManager;

    public DefaultApplePaymentUtil(String inAppRestURL,
                                   PaymentHistoryDAO paymntHistDAO,
                                   ITunePaymentLogDAO iTuneDAO,
                                   RestUtil restUtil,
                                   PricingPackageDAO pricePkgDAO,
                                   UserDAO userDAO,
                                   BillingManager billingManager,
                                   PaymentManager paymentMgr,
                                   String inAppRestTestURL,
                                   NotificationEngine notificationEngine) {

        super(paymntHistDAO, pricePkgDAO, userDAO, billingManager, notificationEngine);
        this.inAppRESTURL = inAppRestURL;
        this.inAppRESTTestURL = inAppRestTestURL;
        this.iTunePymntLogDAO = iTuneDAO;
        this.restUtil = restUtil;
        this.paymentManager = paymentMgr;
        init();
        APPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        APPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
    }

    public void processPaymentWithVerifiedReceipt(JSONObject verifiedReceipt, UserVO userVO) throws CoreException {
        ItunePaymentLogVO paymentLogVO = new ItunePaymentLogVO();
        paymentLogVO.setUserVO(userVO);
        paymentLogVO.setReceiveTime(new Date());
        paymentLogVO.setReceipt(verifiedReceipt.toString());
        paymentLogVO.setMarket(userVO.getMarketVO().getName());
        iTunePymntLogDAO.create(paymentLogVO);
        try {
            String premiumPkgCode = verifiedReceipt.getString(APPLE_PRODUCT_ID_KEY);
            Date nextPaymentDate = null;
            if (verifiedReceipt.has(APPLE_EXPIRE_DATE_KEY)) {
                nextPaymentDate = new Date(new Long(verifiedReceipt.getString(APPLE_EXPIRE_DATE_KEY)));
            }
            String remark = null;
            if (verifiedReceipt.has(APPLE_ORG_TRANSACTION_ID_KEY)) {
                remark = APPLE_ORG_TRANSACTION_ID_KEY + ":"
                        + verifiedReceipt.getString(APPLE_ORG_TRANSACTION_ID_KEY);
            }
            activateUserPremiumPackage(premiumPkgCode, userVO, nextPaymentDate, applePaymentGW, remark);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void processPayment(String receiptData, UserVO userVO) throws CoreException {
        myLog.debug("Processing payment for->" + userVO.getUserid() + ", receiptData" + receiptData);
        // JK 27-Apr-2012
        //{  "status" : 0,"receipt" : { (receipt here) }}

        try {
            insertNewITunePaymentLog(userVO, receiptData);

            // For testing purpose 
            JSONObject verifyReceiptResultObj = submitVerifyReceiptToApple(receiptData);
            if (verifyReceiptResultObj != null) {

                /*
                 * This is for the auto-renew receipt
                 *
                 * {
                 * "latest_receipt":"ewoJInNpZ25hdHVyZSIgPSAiQXFCUU1WWFU4NDVzOUMrTGFXQndsU0JUaUNkamM3OU93ZWxuajRYdWtTRWRiZXJQRFVmaE94bVcvRHlrV2kyUk5ZMDFZdkRJTm9SbDQ0YU01aVdCcGJtQWNUZzE2SXFieWRvMFdTQ29KcUtyTFIzNVhTd0VjcTJCWklXV1J6YUhiSU40ZTZVVmZaZ3lwUEk4RlJTSW5kRitaL1E0ejdhUm5WczAzRGZKWml3TkFBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW5GMVlXNTBhWFI1SWlBOUlDSXhJanNLQ1NKd2RYSmphR0Z6WlMxa1lYUmxJaUE5SUNJeU1ERXhMVEE1TFRJM0lEQTRPakU1T2pFeElFVjBZeTlIVFZRaU93b0pJbWwwWlcwdGFXUWlJRDBnSWpRMk5ESXpNalU0TVNJN0Nna2laWGh3YVhKbGN5MWtZWFJsTFdadmNtMWhkSFJsWkNJZ1BTQWlNakF4TVMwd09TMHlOeUF3T0RveU5Eb3hNU0JGZEdNdlIwMVVJanNLQ1NKbGVIQnBjbVZ6TFdSaGRHVWlJRDBnSWpFek1UY3hNVEU0TlRFek16Z2lPd29KSW5CeWIyUjFZM1F0YVdRaUlEMGdJbEJGWDBSR1gxa3hUU0k3Q2draWRISmhibk5oWTNScGIyNHRhV1FpSUQwZ0lqRXdNREF3TURBd01EZzRNalkxTVRjaU93b0pJbTl5YVdkcGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVaUlEMGdJakl3TVRFdE1Ea3RNamNnTURjNk1USTZNVFFnUlhSakwwZE5WQ0k3Q2draWIzSnBaMmx1WVd3dGRISmhibk5oWTNScGIyNHRhV1FpSUQwZ0lqRXdNREF3TURBd01EZzRNakUzT0RNaU93b0pJbUpwWkNJZ1BTQWliV1V1YzJWamNTNWpiR2xsYm5RaU93b0pJbUoyY25NaUlEMGdJak1pT3dwOSI7CgkiZW52aXJvbm1lbnQiID0gIlNhbmRib3giOwoJInBvZCIgPSAiMTAwIjsKCSJzaWduaW5nLXN0YXR1cyIgPSAiMCI7Cn0=",
                 * "status":0, "latest_receipt_info": {
                 * "original_transaction_id":"1000000008821783",
                 * "product_id":"PE_DF_Y1M", "item_id":"464232581",
                 * "original_purchase_date":"2011-09-27 07:12:14 Etc/GMT",
                 * "expires_date":"1317111851338", "quantity":"1",
                 * "purchase_date":"2011-09-27 08:19:11 Etc/GMT",
                 * "bvrs":"3","bid":"me.secq.client",
                 * "expires_date_formatted":"2011-09-27 08:24:11 Etc/GMT",
                 * "transaction_id":"1000000008826517"}, "receipt": {
                 * "original_transaction_id":"1000000008821783",
                 * "product_id":"PE_DF_Y1M", "item_id":"464232581",
                 * "original_purchase_date":"2011-09-27 07:12:14 Etc/GMT",
                 * "expires_date":"1317111851338",
                 * "quantity":"1","purchase_date":"2011-09-27 08:19:11 Etc/GMT",
                 * "bvrs":"3","bid":"me.secq.client",
                 * "expires_date_formatted":"2011-09-27 08:24:11 Etc/GMT",
                 * "transaction_id":"1000000008826517"}}
                 *
                 * For in-app one time "receipt": {
                 * "original_transaction_id":"1000000008821783",
                 * "product_id":"PE_DF_Y1M", "item_id":"464232581",
                 * "original_purchase_date":"2011-09-27 07:12:14 Etc/GMT",
                 * "expires_date":"1317111851338",
                 * "quantity":"1","purchase_date":"2011-09-27 08:19:11 Etc/GMT",
                 * "bvrs":"3","bid":"me.secq.client",
                 * "expires_date_formatted":"2011-09-27 08:24:11 Etc/GMT",
                 * "transaction_id":"1000000008826517"}}
                 *
                 * This is for the re-purchase
                 *
                 * "receipt":{"request_date_pst":"2014-04-09 02:36:37 America/Los_Angeles",
                 *            "receipt_type":"ProductionSandbox",
                 *     "in_app":[{"original_purchase_date_pst":"2014-04-08 22:25:14 America/Los_Angeles",
                 *     "original_transaction_id":"1000000107249248","is_trial_period":"false",
                 *     "product_id":"1_PP_3M","original_purchase_date_ms":"1397021114000",
                 *     "original_purchase_date":"2014-04-09 05:25:14
                 *
                 */
                ItunePaymentLogVO paymentLogVO =
                        updateItuneVerifyResult(receiptData, verifyReceiptResultObj);

                JSONObject receiptObj = new JSONObject(paymentLogVO.getReceipt());
                String premiumPkgCode = receiptObj.getString(APPLE_PRODUCT_ID_KEY);
                Date nextPaymentDate = null;
                if (receiptObj.has(APPLE_EXPIRE_DATE_KEY)) {
                    nextPaymentDate = new Date(new Long(receiptObj.getString(APPLE_EXPIRE_DATE_KEY)));
                }
                String remark = null;
                if (receiptObj.has(APPLE_ORG_TRANSACTION_ID_KEY)) {
                    remark = APPLE_ORG_TRANSACTION_ID_KEY + ":"
                            + receiptObj.getString(APPLE_ORG_TRANSACTION_ID_KEY);
                }
                activateUserPremiumPackage(premiumPkgCode, userVO, nextPaymentDate, applePaymentGW, remark);
            }
        } catch (JSONException je) {
            myLog.error("JSONError ->" + je.getMessage(), je);
        } catch (Exception exp) {
            myLog.error("Problem of processing Apple Payment>", exp);
            exp.printStackTrace();
        }

    }

    private ItunePaymentLogVO updateItuneVerifyResult(String receiptData, JSONObject receiptObj) {
        // needs to handle with purchase again
        //
        // {"environment":"Sandbox","status":0,
        // "receipt":{"request_date_pst":"2014-04-09 02:36:37 America/Los_Angeles",
        //            "receipt_type":"ProductionSandbox",
        //            "in_app":[{"original_purchase_date_pst":"2014-04-08 22:25:14 America/Los_Angeles",
        //            "original_transaction_id":"1000000107249248","is_trial_period":"false",
        //            "product_id":"1_PP_3M","original_purchase_date_ms":"1397021114000",
        //            "original_purchase_date":"2014-04-09 05:25:14

        ItunePaymentLogVO paymentLogVO = iTunePymntLogDAO.findByReceiptData(receiptData);
        try {
            if (paymentLogVO != null) {
                paymentLogVO.setVerifyResult(receiptObj.toString());
                if (receiptObj.has(APPLE_STATUS_KEY)) {
                    paymentLogVO.setStatus(receiptObj.getInt(APPLE_STATUS_KEY));
                }

                if (receiptObj.has(APPLE_RECEIPT_KEY)) {
                    // Check if user have multiple receipts, if yes get the latest receipt
                    JSONObject receipt = receiptObj.getJSONObject(APPLE_RECEIPT_KEY);
                    if (receipt.has("in_app")) {
                        // is the array of receiptObj
                        JSONArray receiptArray = receipt.getJSONArray("in_app");
                        JSONObject latestReceipt = null;
                        Long transateDate = 0l;
                        JSONObject tempReceipt = null;
                        for (int i = 0; i < receiptArray.length(); i++) {
                            tempReceipt = receiptArray.getJSONObject(i);
                            Long tempTransacDate = new Long(tempReceipt.getString("original_purchase_date_ms"));
                            if (tempTransacDate > transateDate) {
                                transateDate = tempTransacDate;
                                latestReceipt = tempReceipt;
                            }
                        }

                        paymentLogVO.setReceipt(latestReceipt.toString());
                    } else {
                        paymentLogVO.setReceipt(receiptObj.getString(APPLE_RECEIPT_KEY));
                    }
                }

                if (receiptObj.has(APPLE_LATEST_RECEIPT_KEY)) {
                    paymentLogVO.setLatestReceipt(receiptObj.getString(APPLE_LATEST_RECEIPT_KEY));
                }

                if (receiptObj.has(APPLE_LATEST_RECEIPT_INFO_KEY)) {
                    paymentLogVO.setLatestReceiptInfo(receiptObj.getString(APPLE_LATEST_RECEIPT_INFO_KEY));
                }

                paymentLogVO.setLastUpdateTime(new Date());
                iTunePymntLogDAO.update(paymentLogVO);
            }
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return paymentLogVO;
    }

    private void init() {
        SystemProperties sysProp = SystemProperties.instance;
        APPLE_SHARE_SECRET = sysProp.getString("itune.inapp.share.secret.default");
        applePaymentGW = paymentManager.getPaymentGW(APPLE_GW_NAME);
        myLog.debug("Apple Payment Gateway initialized!");
    }

    private void insertNewITunePaymentLog(UserVO userVO, String receiptData) {
        ItunePaymentLogVO paymentLogVO = new ItunePaymentLogVO();
        paymentLogVO.setUserVO(userVO);
        paymentLogVO.setReceiptData(receiptData);
        paymentLogVO.setReceiveTime(new Date());
        paymentLogVO.setMarket(userVO.getMarketVO().getName());

        this.iTunePymntLogDAO.create(paymentLogVO);
    }

    private JSONObject submitVerifyReceiptToApple(String receiptData) {
        JSONObject resultObj = null;
        try {

            JSONObject reqObj = new JSONObject();
            reqObj.put(APPLE_RECEIPT_DATA_KEY, receiptData);
            reqObj.put(APPLE_SHARE_SECRET_KEY, APPLE_SHARE_SECRET);
            myLog.debug("Submittig req->" + reqObj.toString() + " to apple itune");
            String httpResult = restUtil.executePost(inAppRESTURL, reqObj.toString(), null);
            myLog.debug("Apple iTune Verify Receipt result->" + httpResult);
            resultObj = new JSONObject(httpResult);
            if (resultObj.has(APPLE_STATUS_KEY)) {
                if (resultObj.getInt(APPLE_STATUS_KEY) == 21007) {//21007 represent ox, verify with sand
                    myLog.debug("send to sandbox to verify because Apple iTune Verify Receipt status key is " + resultObj.getInt(APPLE_STATUS_KEY));
                    httpResult = restUtil.executePost(inAppRESTTestURL, reqObj.toString(), null);
                    myLog.debug("sandbox result->" + httpResult);
                    resultObj = new JSONObject(httpResult);
                }
            }
        } catch (RestExecException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return resultObj;
    }
}

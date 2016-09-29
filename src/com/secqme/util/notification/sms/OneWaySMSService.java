package com.secqme.util.notification.sms;

import com.secqme.domain.model.notification.sms.SMSLogVO;
import com.secqme.domain.model.notification.sms.SmsGateWayVO;
import com.secqme.util.TextUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URLEncoder;

/**
 *
 * @author jameskhoo
 */
@Deprecated
public class OneWaySMSService implements SMSService {

    private RestUtil restUtil = null;
    private String baseURL = null;
    private static Logger myLog = Logger.getLogger(OneWaySMSService.class);
    public final static int NON_UNICODE_MSG_LENGTH = 160;
    public final static int UNICODE_MSG_LENGTH = 70;
    private final static int ERROR_SMS_LANGAUGE = -400; //languagetype is invalid
    private final static int ERROR_SMS_INVALID_CHARACTER = -500; //Invalid characters in message
    private final static int ERROR_SMS_NOT_ENOUGH_CREDIT_ERROR = -600; //Insufficient credit balance

    private TextUtil textUtil = null;

    public OneWaySMSService() {
        // Empty Constructor
    }

    @Deprecated
    public SMSLogVO sendSMS(String countryCode, String mobileNumber, String msg) throws SMSSendException {

        myLog.debug("Sending msg:" + msg + " to " + countryCode + mobileNumber);
        SMSLogVO smsLogVO = new SMSLogVO();
        String langToken = "&languageType=1";
        if (textUtil.msgContainsUnicode(msg)) {
            langToken = "&languageType=2";
            if (msg.length() >= UNICODE_MSG_LENGTH) {
                msg = msg.substring(0, UNICODE_MSG_LENGTH - 4) + "..";
            }
            msg = textUtil.convertToHex(msg);
        } else {
            if (msg.length() >= NON_UNICODE_MSG_LENGTH) {
                msg = msg.substring(0, NON_UNICODE_MSG_LENGTH - 6) + "..";
            }
        }

        String msisdn = countryCode + mobileNumber;
        if (msisdn.startsWith("+")) {
            msisdn = msisdn.substring(1);
        }
        String msgToSend = URLEncoder.encode(msg);
        String msgToken = "&message=" + msgToSend;
        String mobileToken = "&mobileno=" + msisdn;
        String finalURL = baseURL + mobileToken + langToken + msgToken;
        myLog.debug("finalURL is -->" + finalURL);
        String smsResult = null;
        try {
            smsResult = restUtil.executeGet(finalURL, null);
            int resultCode = Integer.parseInt(smsResult);
            smsLogVO.setStatus(smsResult);
            smsLogVO.setTransactionId("" + resultCode);
            if(resultCode < 0 ) {
                String errMsg = null;
                switch (resultCode) {
                    case ERROR_SMS_LANGAUGE:
                        errMsg = "Invalid Language Set";
                        break;
                    case ERROR_SMS_INVALID_CHARACTER:
                        errMsg = "Invalid SMS Character";
                        break;
                    case ERROR_SMS_NOT_ENOUGH_CREDIT_ERROR:
                        errMsg = "Not Enuf Credit Error";
                        break;

                }
                if (errMsg != null) {
                    smsLogVO.setErrorMessage(errMsg);
                    throw new SMSSendException("Problem of sending this sms, msg" + msg + ", mobile: " + mobileNumber +
                                               "error:" + errMsg);
                }
            }


        } catch (RestExecException re) {
            throw new SMSSendException("Problem of execute Rest Service Call when sending this SMS, msg: " + msg + ", mobile number: " + mobileNumber, re);
        } catch (NumberFormatException ne) {
            // is ok
        }
        myLog.debug("Result of sending sms to : " + mobileNumber + " is->" + smsResult);

        return smsLogVO;
    }


    public void init(SmsGateWayVO smsGWVO, RestUtil restUtil, TextUtil txtUtil, MobileNumberUtil mobileNumberUtil) {
        this.restUtil = restUtil;
        this.textUtil = txtUtil;
         
        //{"sms.api.user.name":"APIH1ZXDCAERS",
        // "sms.api.password":"APIH1ZXDCAERS5BTI5",
        // "sms.api.send.url":"http://gateway.onewaysms.com.my:10001/api.aspx","sms.transaction.url":"http://gateway.onewaysms.com.my:10001/bulktrx.aspx"}
        
        try {
            JSONObject smsConfig = new JSONObject(smsGWVO.getAdditionalConfig());
            String usrName = smsConfig.getString("sms.api.user.name");
            String pass = smsConfig.getString("sms.api.password");
            String sendURL = smsConfig.getString("sms.api.send.url");
            this.baseURL = sendURL + "?apiusername=" + usrName + "&apipassword=" + pass + "&senderid=secQme";
            myLog.debug(OneWaySMSService.class.getName() + " initialized, the baseURL is set to: " + this.baseURL);
            this.restUtil = restUtil;

        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }
}

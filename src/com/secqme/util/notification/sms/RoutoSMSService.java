package com.secqme.util.notification.sms;

import com.secqme.domain.model.notification.sms.SMSLogVO;
import com.secqme.domain.model.notification.sms.SmsGateWayVO;
import com.secqme.util.TextUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import java.net.URLEncoder;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author jameskhoo
 */
@Deprecated
public class RoutoSMSService implements SMSService {

    private RestUtil restUtil = null;
    private String baseURL = null;
    private static Logger myLog = Logger.getLogger(RoutoSMSService.class);
    public final static int NON_UNICODE_MSG_LENGTH = 160;
    public final static int UNICODE_MSG_LENGTH = 70;
    private TextUtil textUtil = null;

    public RoutoSMSService() {
        // Empty Constructor
    }

    @Deprecated
    public SMSLogVO sendSMS(String countryCode, String mobileNumber,
                          String msg) throws SMSSendException {

        SMSLogVO smsResultVO = new SMSLogVO();
        myLog.debug("Sending msg:" + msg + " to " + countryCode + mobileNumber);
        if (textUtil.msgContainsUnicode(msg)) {
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
        String mobileToken = "&number=" + msisdn;
        String finalURL = baseURL + mobileToken  + msgToken;
        myLog.debug("finalURL is -->" + finalURL);
        String smsResult = null;
        try {
            smsResult = restUtil.executeGet(finalURL, null);
            smsResultVO.setStatus(smsResult);
            smsResultVO.setTransactionId(smsResult);
        } catch (RestExecException re) {
            throw new SMSSendException("Problem of execute Rest Service Call when sending this SMS, msg:" + msg +
                                        ", mobile number:" + mobileNumber, re);
        } 
        myLog.debug("Result of sending sms to : " + mobileNumber + " is->" + smsResult);

        return smsResultVO;
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
            this.baseURL = sendURL + "?user=" + usrName + "&pass=" + pass + "&ownnum=secQme";
            myLog.debug(RoutoSMSService.class.getName() + " initialized, the baseURL is set to->" + this.baseURL);
            this.restUtil = restUtil;
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }
}

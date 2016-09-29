package com.secqme.util.notification.sms;

import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.api.response.message.MessageResponse;
import com.plivo.helper.exception.PlivoException;
import com.secqme.domain.model.notification.sms.SMSLogVO;
import com.secqme.domain.model.notification.sms.SmsGateWayVO;
import com.secqme.util.TextUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;
import java.util.LinkedHashMap;

public class PlivoSMSService implements SMSService {

    public static final String PLIVO_GATEWAY_NAME = "plivo";

    private static Logger myLog = Logger.getLogger(PlivoSMSService.class);
    private static String authId;
    private static String orgMobileNumber;
    private static String authToken;
    private static RestAPI api;
    private static MobileNumberUtil mobileNumberUtil;

    public PlivoSMSService() {
        // Empty Constructor
    }

    public SMSLogVO sendSMS(String countryCallingCode, String mobileNumber, String msg) throws SMSSendException {
        LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
        String updatedMobileNumber = mobileNumberUtil.checkAndReturnCorrectMobileNumber(
        		mobileNumber, Integer.valueOf(countryCallingCode));

        String msisdn = countryCallingCode + mobileNumber;
        if (!msisdn.startsWith("+")) {
            msisdn = "+" + msisdn;
        }
        myLog.debug("Sending SMS via Plivo from: " + orgMobileNumber +" to: " + msisdn + ", msg: " + msg);

        parameters.put("src", orgMobileNumber);
        parameters.put("dst", countryCallingCode + updatedMobileNumber);
        parameters.put("text", msg);

        SMSLogVO smsLogVO = new SMSLogVO();
        smsLogVO.setGwName(PLIVO_GATEWAY_NAME);
        smsLogVO.setMobileNo(msisdn);
        smsLogVO.setMessage(msg);
        smsLogVO.setSendTime(new Date());

        try {
            api = new RestAPI(authId, authToken, "v1");
            MessageResponse msgResponse = api.sendMessage(parameters);
            smsLogVO.setStatus(msgResponse.serverCode.toString());
            System.out.println(msgResponse.apiId);
            if (msgResponse.serverCode == 202) {
                smsLogVO.setTransactionId(msgResponse.messageUuids.get(0).toString());
                smsLogVO.setSendOut(true);
            } else {
                smsLogVO.setSendOut(false);
                smsLogVO.setErrorMessage(msgResponse.message);
            }
        } catch (PlivoException ex) {
            myLog.error("Problem on sending SMS via Twillio to: " + msisdn + ", error: " + ex.getMessage(), ex);
            smsLogVO.setSendOut(false);
        }

        return smsLogVO;
    }


    public void init(SmsGateWayVO smsGWVO, RestUtil restUtil, TextUtil txtUtil, MobileNumberUtil mobileNumberUtil) {
    	try {
            JSONObject smsConfig = new JSONObject(smsGWVO.getAdditionalConfig());
            authId = smsConfig.getString("sms.auth.id");
            authToken = smsConfig.getString("sms.auth.token");
            orgMobileNumber = smsConfig.getString("sms.mobile.number");
            api = new RestAPI(authId, authToken, "v1");
            this.mobileNumberUtil = mobileNumberUtil;

            myLog.debug("Finish to initialize SMS Gateway: " + PlivoSMSService.class.getName());
        } catch (JSONException ex) {
            myLog.error("Failed to initialize SMS Gateway: " + PlivoSMSService.class.getName(), ex);
        }
    }
}
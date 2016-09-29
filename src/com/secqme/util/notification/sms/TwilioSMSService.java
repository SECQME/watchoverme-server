package com.secqme.util.notification.sms;

import com.secqme.domain.model.notification.sms.SMSLogVO;
import com.secqme.domain.model.notification.sms.SmsGateWayVO;
import com.secqme.util.TextUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.rest.RestUtil;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TwilioSMSService implements SMSService {

    public static final String TWILIO_GATEWAY_NAME = "twilio";
    private static Logger myLog = Logger.getLogger(TwilioSMSService.class);

    private TwilioRestClient client = null;
    private String twilioNumber = null;

    public TwilioSMSService() {
        // Empty Constructor
    }

    public SMSLogVO sendSMS(String countryCallingCode, String mobileNumber,
                          String msg) throws SMSSendException {
        String msisdn = countryCallingCode + mobileNumber;
        if (!msisdn.startsWith("+")) {
            msisdn = "+" + msisdn;
        }
        myLog.debug("Sending SMS via Twilio from: " + twilioNumber +" to: " + msisdn + ", msg:" + msg);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("Body", msg));
        params.add(new BasicNameValuePair("To", msisdn));
        params.add(new BasicNameValuePair("From", twilioNumber));

        SMSLogVO smsLogVO = new SMSLogVO();
        smsLogVO.setGwName(TWILIO_GATEWAY_NAME);
        smsLogVO.setMobileNo(msisdn);
        smsLogVO.setMessage(msg);
        smsLogVO.setSendTime(new Date());

		try {
            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message message = messageFactory.create(params);
            smsLogVO.setTransactionId(message.getSid());
            smsLogVO.setStatus(message.getStatus());
            smsLogVO.setSendOut(true);
		} catch (TwilioRestException ex) {
			myLog.error("Problem on sending SMS via Twilio to: " + countryCallingCode + "-" + mobileNumber +
                    " error: " + ex.getMessage() + ", code: " + ex.getErrorCode() + ", additional info: " + ex.getMoreInfo(), ex);

            smsLogVO.setErrorMessage(ex.getErrorMessage());
            smsLogVO.setStatus("failed");
            smsLogVO.setSendOut(false);
		}

        return smsLogVO;
    }

    public void init(SmsGateWayVO smsGWVO, RestUtil restUtil, TextUtil txtUtil, MobileNumberUtil mobileNumberUtil) {
    	try {
            JSONObject smsConfig = new JSONObject(smsGWVO.getAdditionalConfig());
            String usrName = smsConfig.getString("sms.api.user.name");
            String pass = smsConfig.getString("sms.api.password");
            twilioNumber = smsConfig.getString("sms.api.number");
            client = new TwilioRestClient(usrName, pass);

            myLog.debug("Finish to initialize SMS Gateway: " + TwilioSMSService.class.getName());
        } catch (JSONException ex) {
            myLog.error("Failed to initialize SMS Gateway: " + TwilioSMSService.class.getName(), ex);
        }
    }
}
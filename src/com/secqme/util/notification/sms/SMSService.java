package com.secqme.util.notification.sms;

import com.secqme.domain.model.notification.sms.SmsGateWayVO;
import com.secqme.domain.model.notification.sms.SMSLogVO;
import com.secqme.util.TextUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.rest.RestUtil;

/**
 *
 * @author jameskhoo
 */
public interface SMSService {
    public void init(SmsGateWayVO smsGWVO, RestUtil restUtil, TextUtil txtUtil, MobileNumberUtil mobileNumberUtil);
    public SMSLogVO sendSMS(String countryCallingCode, String mobileNumber, String msg) throws SMSSendException;
}
package com.secqme.util.notification.sms;

import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.sms.SMSLogVO;
import com.secqme.domain.model.notification.sms.SMSRecipientVO;
import com.secqme.domain.model.notification.sms.SMSVO;
import com.secqme.domain.model.notification.sms.SmsCountryVO;
import java.util.List;

/**
 *
 * @author jameskhoo
 */
public interface SMSManager {
    public int sendSMS(SMSVO smsVO, OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener, OnAfterSMSLogSavedListener onAfterSMSLogSavedListener);
    public void initAllSMSGateways();
    public int getSMSCreditForCountry(String countryCode);
    public void refreshSetting();
    public CountryVO getCountry(String countryISO);
    public List<SmsCountryVO> getSupportedSMSCountryList();

    public interface OnBeforeSMSLogSavedListener {
        public void onBeforeSMSLogSaved(SMSVO smsVO, SMSRecipientVO smsRecipientVO, SMSLogVO log);
    }

    public interface OnAfterSMSLogSavedListener {
        public void onAfterSMSLogSaved(SMSVO smsVO, SMSRecipientVO smsRecipientVO, SMSLogVO log);
    }
}

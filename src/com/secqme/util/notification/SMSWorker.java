package com.secqme.util.notification;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.sms.SMSVO;
import com.secqme.util.notification.sms.SMSManager;
import com.secqme.util.notification.sms.SMSSendException;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * A Thread working of sending email in background thread
 *
 * @author james
 */
public class SMSWorker implements Callable {

    private final static Logger myLog = Logger.getLogger(SMSWorker.class);
    private SMSManager smsManager;
    private SMSVO smsVO;
    private SMSManager.OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener;
    private SMSManager.OnAfterSMSLogSavedListener onAfterSMSLogSavedListener;

    public SMSWorker(SMSManager smsManager, SMSVO smsVO, SMSManager.OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener, SMSManager.OnAfterSMSLogSavedListener onAfterSMSLogSavedListener) {
        this.smsManager = smsManager;
        this.smsVO = smsVO;
        this.onBeforeSMSLogSavedListener = onBeforeSMSLogSavedListener;
        this.onAfterSMSLogSavedListener = onAfterSMSLogSavedListener;
    }

    public Integer call() throws Exception {
        myLog.debug("Thread: " + Thread.currentThread().getName() + " attempt to send sms: " + smsVO);
        return smsManager.sendSMS(smsVO, onBeforeSMSLogSavedListener, onAfterSMSLogSavedListener);
    }

}

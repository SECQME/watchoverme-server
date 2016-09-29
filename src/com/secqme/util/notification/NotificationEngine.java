package com.secqme.util.notification;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.notification.email.EmailVO;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.push.PushMessageVO;
import com.secqme.domain.model.notification.sms.SMSRecipientVO;
import com.secqme.domain.model.notification.sms.SMSVO;
import com.secqme.domain.model.notification.sms.TemplateBasedSMSVO;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.util.ar.ParameterizableUtils;
import com.secqme.util.marketing.MarketingCampaign;
import com.secqme.util.marketing.MarketingEmailSubscribeWorker;
import com.secqme.util.marketing.MarketingEmailUnsubscribeWorker;
import com.secqme.util.notification.email.EmailService;
import com.secqme.util.notification.sms.DefaultSMSManager;
import com.secqme.util.notification.sms.SMSManager;

import java.util.Date;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * This is the main interface of Notification
 * of email or sms services
 *
 * @author james
 */
public interface NotificationEngine {

    public void sendEmail(EmailVO emailVO, EmailService.OnBeforeEmailLogSavedListener listener);
    public void sendPushMessage(PushMessageVO pushMessageVO);
    public void sendSMS(SMSVO smsVO, SMSManager.OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener, SMSManager.OnAfterSMSLogSavedListener onAfterSMSLogSavedListener);
    public void publishSNSMessage(SNSMessageVO snsMessageVO);

    public void sendWelcomeEmail(final UserVO userVO);
    public void sendWelcomeEmail(final UserVO userVO, BillingPkgType billingPkgType);

    public void sendEmailVerification(final UserVO userVO);
    public void sendEmailChangedVerification(final UserVO userVO);

    @Deprecated // For old email; Send a new random plain password
    public void sendPasswordReset(final UserVO user, final String newPassword);
    public void sendPasswordResetPin(UserVO userVO, String pin, boolean sendEmail, boolean sendSMS);
    public void sendPasswordResetTokenLink(final UserVO userVO);

    public void sendContactInvitation(final ContactVO contactVO, boolean sendEmail, boolean sendSMS);
    public void sendContactInvitationReminder(final ContactVO contactVO, boolean sendEmail);
    public void sendContactRejectedNotification(final ContactVO contactVO, MessageType messageType);
    public void sendContactMisconfiguredEmail(ContactVO contactVO); // Send only to email based user?

    public int notifyEmergencyEvent(SecqMeEventVO secqMeEventVO, int smsCreditBalance);
    public int notifyExpireEvent(SecqMeEventVO event, int smsCreditBalance);
    public int notifySafetyConfirmationEvent(SecqMeEventVO eventVO, int smsCreditBalance);
    public int sendShareEventEmailAndSms(SecqMeEventVO eventVO, int smsCreditBalance, List<ContactVO> contactList);

    public void sendMobileVerificationSMS(String market, String langCode, CountryVO countryVO, String mobileNumber, String pin);
    public void notifyTrialEvent(SecqMeEventVO eventVO, String market, String langCode, CountryVO countryVO, String mobileNumber);

    public void sendWatchOverMeNowTOSNS(SecqMeEventVO event);
    public int notifyContactOnSafelyArrival(SecqMeEventVO eventVO, int smsCreditBalance, boolean  toSendSMS, boolean toSendEmail, boolean toSendfacebook);

    public void sendThanksGifting(String market, String langCode, GiftPaymentLogVO giftPaymentLogVO);
    public void sendEventPushNotification(UserVO fromUserVO, UserVO toUserVO, PushNotificationEventType pushNotificationEventType, SecqMeEventVO eventVO);

    @Deprecated
    public void notifyContactToUpdate(UserVO userVO, List<ContactVO> contactList);
    @Deprecated
    public void resendNotificationToContact(UserVO userVO, ContactVO contactVO);

    @Deprecated // Empty implementation
    public void sendNonRecurringBillEndingReminder(UserVO userVO, String pkgDescription, Date endDate);

    public void sendTestHtmlEmailSms(String name, String emailAddress, CountryVO countryVO, String number);

    @Deprecated // Not used
    public void testSNS(UserVO userVO, String message);

    public void addMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns);
    public void addMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns);
    public void deleteMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns);
    public void deleteMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns);
}

package com.secqme.util.notification;

import com.secqme.domain.factory.notification.EmailRecipientVOFactory;
import com.secqme.domain.factory.notification.PushMessageRecipientVOFactory;
import com.secqme.domain.factory.notification.SMSRecipientVOFactory;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserIdType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.event.EventType;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.email.*;
import com.secqme.domain.model.notification.push.PushMessageVO;
import com.secqme.domain.model.notification.push.TemplateBasedPushMessageVO;
import com.secqme.domain.model.notification.sms.*;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import com.secqme.domain.model.notification.sns.TemplateBasedSNSMessageVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.util.ar.ParameterizableUtils;
import com.secqme.util.marketing.MarketingCampaign;
import com.secqme.util.marketing.MarketingEmailService;
import com.secqme.util.marketing.MarketingEmailSubscribeWorker;
import com.secqme.util.marketing.MarketingEmailUnsubscribeWorker;
import com.secqme.util.notification.email.EmailService;
import com.secqme.util.notification.email.EmailWorker;
import com.secqme.util.notification.push.PushService;
import com.secqme.util.notification.sms.SMSManager;
import com.secqme.util.notification.sns.SNSManager;
import com.secqme.util.notification.sns.SNSService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author james
 */
public class DefaultNotificationEngine implements NotificationEngine {

    private static Logger myLog = Logger.getLogger(DefaultNotificationEngine.class);

    private final ExecutorService threadPool;

    private final SMSManager smsManager;
    private final SNSManager snsManager;
    private final EmailService emailService;
    private final PushService pushService;
    private HashMap<String, SNSService> snsServiceHashMap = null;
    private MarketingEmailService marketingEmailService = null;

    public DefaultNotificationEngine(SMSManager smsManager, SNSManager snsManager, EmailService emailService, PushService pushService, MarketingEmailService marketingEmailService) {
        this.threadPool = Executors.newCachedThreadPool();

        this.smsManager = smsManager;
        this.snsManager = snsManager;
        this.emailService = emailService;
        this.pushService = pushService;
        this.marketingEmailService = marketingEmailService;
    }

    @Override
    public void sendEmail(EmailVO emailVO, EmailService.OnBeforeEmailLogSavedListener listener) {
        FutureTask task = new FutureTask(new EmailWorker(emailService, emailVO, listener));
        threadPool.submit(task);
    }

    @Override
    public void sendPushMessage(PushMessageVO pushMessageVO) {
        FutureTask task = new FutureTask(new PushMessageWorker(pushService, pushMessageVO));
        threadPool.submit(task);
    }

    @Override
    public void sendSMS(SMSVO smsVO, SMSManager.OnBeforeSMSLogSavedListener onBeforeSMSLogSavedListener, SMSManager.OnAfterSMSLogSavedListener onAfterSMSLogSavedListener) {
        FutureTask task = new FutureTask(new SMSWorker(smsManager, smsVO, onBeforeSMSLogSavedListener, onAfterSMSLogSavedListener));
        threadPool.submit(task);
    }

    @Override
    public void publishSNSMessage(SNSMessageVO snsMessageVO) {
        FutureTask task = new FutureTask(new SNSWorker(snsManager, snsMessageVO));
        threadPool.submit(task);
    }

    @Override
    public void sendWelcomeEmail(final UserVO userVO) {
        sendWelcomeEmail(userVO, userVO.getPackageVO().getPkgType());
    }

    @Override
    public void sendWelcomeEmail(final UserVO userVO, BillingPkgType billingPkgType) {
        MessageType messageType;
        switch (billingPkgType) {
            case LITE:
                messageType = MessageType.WELCOME_LITE_PACKAGE;
                break;
            case PREMIUM:
                messageType = MessageType.WELCOME_PREMIUM_PACKAGE;
                break;
            default:
                throw new IllegalArgumentException("Unknown billing package: " + billingPkgType);
        }

        EmailRecipientVO emailRecipientVO = EmailRecipientVOFactory.createToUserVO(userVO);
        TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(messageType,
                emailRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
        sendEmail(emailVO, null);
    }

    @Override
    public void sendEmailVerification(final UserVO userVO) {
        EmailRecipientVO emailRecipientVO = EmailRecipientVOFactory.createToUserVO(userVO);

        TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.EMAIL_VERIFICATION,
                emailRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
        sendEmail(emailVO, new EmailLogFiller(userVO, userVO, null, null));
    }

    @Override
    public void sendEmailChangedVerification(final UserVO userVO) {
        EmailRecipientVO emailRecipientVO = EmailRecipientVOFactory.createToUserVO(userVO);

        TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.EMAIL_CHANGED_VERIFICATION,
                emailRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
        sendEmail(emailVO, new EmailLogFiller(userVO, userVO, null, null));
    }

    @Override
    public void sendPasswordReset(final UserVO userVO, final String newPassword) {
        myLog.debug("Sending password reset email to user: " + userVO.getUserid());

        EmailRecipientVO emailRecipientVO = EmailRecipientVOFactory.createToUserVO(userVO);
        ParameterizableUtils.fillParamsWithPin(emailRecipientVO, newPassword);

        TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.PASSWORD_RESET,
                emailRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
        sendEmail(emailVO, new EmailLogFiller(userVO, userVO, null, null));
    }

    @Override
    public void sendPasswordResetPin(final UserVO userVO, String pin, boolean sendEmail, boolean sendSMS) {
        myLog.debug("Sending password reset pin to user: " + userVO.getUserid() + ", email: " + sendEmail + ", sms: " + sendSMS);

        if (sendEmail) {
            EmailRecipientVO emailRecipientVO = EmailRecipientVOFactory.createToUserVO(userVO);
            ParameterizableUtils.fillParamsWithPin(emailRecipientVO, pin);

            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.PASSWORD_RESET_VERIFICATION_PIN,
                    emailRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
            sendEmail(emailVO, new EmailLogFiller(null, userVO, null, null));
        }

        if (sendSMS) {
            SMSRecipientVO smsRecipientVO = SMSRecipientVOFactory.createToUserVO(userVO);
            ParameterizableUtils.fillParamsWithPin(smsRecipientVO, pin);

            TemplateBasedSMSVO smsVO = new TemplateBasedSMSVO(MessageType.PASSWORD_RESET_VERIFICATION_PIN,
                    smsRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
            sendSMS(smsVO, new SMSLogFiller(null, userVO, null, null), null);
        }
    }

    @Override
    public void sendPasswordResetTokenLink(final UserVO userVO) {
        myLog.debug("Sending password reset token link to user: " + userVO.getUserid());

        EmailRecipientVO emailRecipientVO = EmailRecipientVOFactory.createToUserVO(userVO);

        TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.PASSWORD_RESET_TOKEN_LINK,
                emailRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
        sendEmail(emailVO, new EmailLogFiller(null, userVO, null, null));
    }

    @Override
    public void sendContactInvitation(final ContactVO contactVO, boolean sendEmail, boolean sendSMS) {
        final UserVO userVO = contactVO.getUserVO();

        myLog.debug("Sending contact invitation for user: " + userVO.getUserid()
                + " for contact: " + contactVO.getAliasName() + " email:" + contactVO.getEmailAddress()
                + " sendEmail:" + sendEmail + ", sendSMS:" + sendSMS);

        if (sendEmail && StringUtils.isNotEmpty(contactVO.getEmailAddress())) {
            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.CONTACT_INVITATION, EmailRecipientVOFactory.createToContactVO(contactVO), userVO.getMarketVO().getName(), userVO.getLangCode());
            sendEmail(emailVO, new EmailLogFiller(userVO, contactVO.getContactUserVO(), contactVO, null));
        }

        if (sendSMS && StringUtils.isNotEmpty(contactVO.getMobileNo())) {
            TemplateBasedSMSVO smsVO = new TemplateBasedSMSVO(MessageType.CONTACT_INVITATION, SMSRecipientVOFactory.createToContactVO(contactVO), userVO.getMarketVO().getName(), userVO.getLangCode());
            sendSMS(smsVO, new SMSLogFiller(userVO, contactVO.getContactUserVO(), contactVO, null), new SendMissedConfigureContact(contactVO));
        }
    }

    @Override
    public void sendContactInvitationReminder(final ContactVO contactVO, boolean sendEmail) {
        final UserVO userVO = contactVO.getUserVO();

        myLog.debug("Sending contact invitation reminder for user: " + userVO.getUserid()
                + " for contact: " + contactVO.getAliasName() + " email:" + contactVO.getEmailAddress()
                + " sendEmail:" + sendEmail);

        if (sendEmail && StringUtils.isNotEmpty(contactVO.getEmailAddress())) {
            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.CONTACT_INVITATION_REMINDER, EmailRecipientVOFactory.createToContactVO(contactVO), userVO.getMarketVO().getName(), userVO.getLangCode());
            sendEmail(emailVO, new EmailLogFiller(userVO, contactVO.getContactUserVO(), contactVO, null));
        }
    }

    @Override
    public void sendContactRejectedNotification(final ContactVO contactVO, MessageType messageType) {
        final UserVO userVO = contactVO.getUserVO();

        String action = null;
        if (messageType == MessageType.LEFT_EMERGENCY_CONTACT) {
            action = "com.secqme.client.andrioid.CONTACT_LEFT";
        } else if (messageType == MessageType.REJECTED_EMERGENCY_CONTACT) {
            action = "com.secqme.client.andrioid.CONTACT_REMOVED";
        } else {
            throw new IllegalArgumentException("Unknown message type for reject contact: " + messageType);
        }

        TemplateBasedPushMessageVO pushMessageVO = new TemplateBasedPushMessageVO(MessageType.LEFT_EMERGENCY_CONTACT, contactVO.getContactUserVO(), PushMessageRecipientVOFactory.createFromContactToUser(contactVO, userVO), userVO.getMarketVO().getName(), userVO.getLangCode());
        pushMessageVO.putPayload("action", action);
        sendPushMessage(pushMessageVO);

        if (StringUtils.isNotEmpty(userVO.getEmailAddress())) {
            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(messageType,
                    EmailRecipientVOFactory.createToUserVO(contactVO), userVO.getMarketVO().getName(), userVO.getLangCode());
            sendEmail(emailVO, new EmailLogFiller(contactVO.getContactUserVO(), userVO, contactVO, null));
        }
    }

    @Override
    public void sendContactMisconfiguredEmail(ContactVO contactVO) {
        UserVO userVO = contactVO.getUserVO();
        if (UserIdType.EMAIL.equals(userVO.getUserIdType())) {
            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.CONTACT_MOBILE_MISCONFIGURED,
                    EmailRecipientVOFactory.createToUserVO(contactVO),
                    userVO.getMarketVO().getName(), userVO.getLangCode());
            sendEmail(emailVO, new EmailLogFiller(null, userVO, contactVO, null));
        }

        // TODO: Send remainder to mobile?
    }

    @Override
    public int notifyEmergencyEvent(SecqMeEventVO event, int smsCreditBalance) {
        myLog.debug("Process of Emergency Event for " + event.getUserVO().getUserid());
        return this.performEventNotification(event, MessageType.EMERGENCY_NOTIFICATION, smsCreditBalance, true, true, true);
    }

    @Override
    public int notifyExpireEvent(SecqMeEventVO event, int smsCreditBalance) {
        myLog.debug("Process of Expire Event for " + event.getUserVO().getUserid());
        return this.performEventNotification(event, MessageType.EVENT_EXPIRE_NOTIFICATION, smsCreditBalance, true, true, true);
    }

    @Override
    public int notifySafetyConfirmationEvent(SecqMeEventVO eventVO, int smsCreditBalance) {
        return this.performEventNotification(eventVO, MessageType.SAFETY_ARRIVAL_NOTIFICATION, smsCreditBalance, true, true, true);
    }

    @Override
    public int notifyContactOnSafelyArrival(SecqMeEventVO eventVO, int smsCreditBalance, boolean sendSms, boolean sendEmail, boolean sendFB) {
        return this.performEventNotification(eventVO, MessageType.SAFETY_ARRIVAL_NOTIFICATION, smsCreditBalance, sendSms, sendEmail, sendFB);
    }

    @Override
    public int sendShareEventEmailAndSms(SecqMeEventVO eventVO, int smsCreditBalance, List<ContactVO> contactList) {
        myLog.debug("sendShareEventEmailAndSms:" + eventVO.getUserVO().getUserid() + ", size:" + contactList.size());
        if (eventVO.isEventRunning()) {
            return this.performEventNotification(eventVO, MessageType.SHARE_EVENT_EMAIL_AND_SMS, smsCreditBalance,
                    true, true, false, contactList);
        } else {
            return this.performEventNotification(eventVO, MessageType.SHARE_EVENT_END_EMAIL_AND_SMS, smsCreditBalance,
                    true, true, false, contactList);
        }
    }

    private int performEventNotification(SecqMeEventVO event, MessageType messageType,
                                         int smsCreditBalance, boolean toSendSMS, boolean toSendEmail, boolean toSendSNS) {
        return performEventNotification(event, messageType, smsCreditBalance, toSendSMS, toSendEmail, toSendSNS, null);
    }

    private int performEventNotification(SecqMeEventVO event, MessageType messageType,
                                         int smsCreditBalance, boolean toSendSMS, boolean toSendEmail, boolean toSendSNS,
                                         List<ContactVO> contactList) {
        UserVO userVO = event.getUserVO();
        myLog.debug("Perform event notification: " + messageType.name() + " for user: " + userVO.getUserid() + ", for contacts: " + contactList);

        int actualSMSSend = 0;

        // Get all user's contacts if not specify
        if (contactList == null) {
            contactList = userVO.getContactList();
        }

        String marketCode = userVO.getMarketVO().getName();
        int maxContactAllows = userVO.getPackageVO().getMaxSMSContactAllow();

        if (toSendEmail) {
            sendEventEmailToContacts(event, messageType, contactList, maxContactAllows);
        }

        if (toSendSMS) {
            actualSMSSend = sendEventSMSToUserContacts(event, messageType, contactList, maxContactAllows, smsCreditBalance);
        }

        if (toSendSNS) {
            updateSNSStatus(event, messageType);
        }

        return actualSMSSend;
    }

    private void sendEventEmailToContacts(SecqMeEventVO eventVO, MessageType messageType, List<ContactVO> contactList, int contactLimit) {
        List<ContactVO> recipientContactVOs = filterEmailRecipientContacts(messageType, contactList, contactLimit);

        UserVO userVO = eventVO.getUserVO();

        for (ContactVO contactVO : recipientContactVOs) {
            myLog.debug(String.format("Send event email: %d, type: %s, to: %s", eventVO.getId(), messageType, contactVO));
            EmailRecipientVO emailRecipientVO = EmailRecipientVOFactory.createToContactVO(contactVO);
            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(messageType, emailRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
            ParameterizableUtils.fillEventParams(emailVO, eventVO);
            this.sendEmail(emailVO, new EmailLogFiller(userVO, contactVO.getContactUserVO(), null, eventVO));
        }
    }

    /**
     * Filter contacts who are eligible to receive event email notification.
     */
    private List<ContactVO> filterEmailRecipientContacts(MessageType messageType, List<ContactVO> contactVOs, int limit) {
        List<ContactVO> filteredContactVOs = new ArrayList<ContactVO>();
        int count = 0;
        for (ContactVO contactVO : contactVOs) {
            if (StringUtils.isNotEmpty(contactVO.getEmailAddress())) {
                boolean contactEnableEmail = false;

                switch (messageType) {
                    case EMERGENCY_NOTIFICATION:
                    case EVENT_EXPIRE_NOTIFICATION:
                        contactEnableEmail = contactVO.getNotifyEmail();
                        break;
                    case SAFETY_ARRIVAL_NOTIFICATION:
                        contactEnableEmail = contactVO.getSafetyNotifyEmail();
                        break;
                    case SHARE_EVENT_EMAIL_AND_SMS:
                        contactEnableEmail = true;
                        break;
                    case SHARE_EVENT_END_EMAIL_AND_SMS:
                        contactEnableEmail = true;
                        break;
                }

                if (contactEnableEmail) {
                    filteredContactVOs.add(contactVO);
                }

                count++;
                if (count >= limit && limit > 0) {
                    break;
                }
            }
        }
        return filteredContactVOs;
    }

    private int sendEventSMSToUserContacts(SecqMeEventVO eventVO, MessageType messageType, List<ContactVO> contactList, int contactLimit, int smsCreditBalance) {
        int actualSMSSend = 0;
        int smsCreditRequire = 0;

        List<ContactVO> recipientContactVOs = filterSMSRecipientContacts(messageType, contactList, contactLimit);

        UserVO userVO = eventVO.getUserVO();

        for (ContactVO contactVO : recipientContactVOs) {
            if (contactVO.getCountryVO() != null) {
                smsCreditRequire = smsManager.getSMSCreditForCountry(contactVO.getCountryVO().getIso());
            }

            if (actualSMSSend < smsCreditBalance && smsCreditRequire != -1 && (smsCreditRequire + actualSMSSend <= smsCreditBalance)) {
                myLog.debug(String.format("Send event sms: %d, type: %s, to: %s", eventVO.getId(), messageType, contactVO));
                SMSRecipientVO smsRecipientVO = SMSRecipientVOFactory.createToContactVO(contactVO);
                TemplateBasedSMSVO smsVO = new TemplateBasedSMSVO(messageType, smsRecipientVO, userVO.getMarketVO().getName(), userVO.getLangCode());
                ParameterizableUtils.fillEventParams(smsVO, eventVO);
                this.sendSMS(smsVO, new SMSLogFiller(userVO, contactVO.getContactUserVO(), null, eventVO), new SendMissedConfigureContact(contactVO));
                actualSMSSend += smsCreditRequire;
            }
        }

        return actualSMSSend;
    }

    private List<ContactVO> filterSMSRecipientContacts(MessageType messageType, List<ContactVO> contactVOs, int limit) {
        List<ContactVO> filteredContactVOs = new ArrayList<ContactVO>();
        int count = 0;
        for (ContactVO contactVO : contactVOs) {
            if (contactVO.getCountryVO() != null && StringUtils.isNotEmpty(contactVO.getMobileNo())) {
                boolean contactEnableSMSSend = false;
                switch (messageType) {
                    case EMERGENCY_NOTIFICATION:
                    case EVENT_EXPIRE_NOTIFICATION:
                        contactEnableSMSSend = contactVO.getNotifySMS();
                        break;
                    case SAFETY_ARRIVAL_NOTIFICATION:
                        contactEnableSMSSend = contactVO.getSafetyNotifySMS();
                        break;
                    case SHARE_EVENT_EMAIL_AND_SMS:
                        //always enable to share
                        contactEnableSMSSend = true;
                        break;
                    case SHARE_EVENT_END_EMAIL_AND_SMS:
                        //always enable to share
                        contactEnableSMSSend = true;
                        break;
                    default:
                        break;
                }

                if (contactEnableSMSSend) {
                    filteredContactVOs.add(contactVO);
                }

                count++;
                if (count >= limit && limit > 0) {
                    break;
                }
            }
        }
        return filteredContactVOs;
    }

    private void updateSNSStatus(SecqMeEventVO event, MessageType messageType) {
        UserVO userVO = event.getUserVO();
        boolean requireNotified = false;
        if (userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
            for (UserSNSConfigVO userSNSConfigVO : userVO.getSnsConfigList()) {
                if (userSNSConfigVO.isNotify()) {
                    requireNotified = true;
                    break;
                }
            }
        }

        if (requireNotified) {
            TemplateBasedSNSMessageVO snsMessageVO = new TemplateBasedSNSMessageVO(messageType, userVO, userVO.getMarketVO().getName(), userVO.getLangCode());
            ParameterizableUtils.fillEventParams(snsMessageVO, event);
            publishSNSMessage(snsMessageVO);
        }
    }

    @Override
    public void sendWatchOverMeNowTOSNS(SecqMeEventVO event) {
        UserVO userVO = event.getUserVO();

        boolean requireNotified = false;
        if (userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
            for (UserSNSConfigVO userSNSConfigVO : userVO.getSnsConfigList()) {
                if (userSNSConfigVO.isNotify()) {
                    requireNotified = true;
                    break;
                }
            }
        }

        if (requireNotified) {
            TemplateBasedSNSMessageVO snsMessageVO = new TemplateBasedSNSMessageVO(MessageType.WATCH_OVER_ME_NOW, userVO, userVO.getMarketVO().getName(), userVO.getLangCode());
            ParameterizableUtils.fillEventParams(snsMessageVO, event);
            publishSNSMessage(snsMessageVO);
        }
    }

    @Override
    public void sendMobileVerificationSMS(String market, String langCode, CountryVO countryVO, String mobileNumber, String pin) {
        SMSRecipientVO smsRecipientVO = new SMSRecipientVO(countryVO, mobileNumber);
        ParameterizableUtils.fillParamsWithPin(smsRecipientVO, pin);

        TemplateBasedSMSVO smsVO = new TemplateBasedSMSVO(MessageType.MOBILE_NO_VERIFICATION, smsRecipientVO, market, langCode);
        this.sendSMS(smsVO, null, null);
    }

    @Override
    public void notifyTrialEvent(SecqMeEventVO eventVO, String market, String langCode, CountryVO countryVO, String mobileNumber) {
        SMSRecipientVO smsRecipientVO = new SMSRecipientVO(countryVO, mobileNumber);
        TemplateBasedSMSVO smsVO = new TemplateBasedSMSVO(MessageType.TRIAL_EVENT_NOTIFICATION, smsRecipientVO, market, langCode);
        ParameterizableUtils.fillEventParams(smsVO, eventVO);
        this.sendSMS(smsVO, new SMSLogFiller(eventVO.getUserVO(), null, null, eventVO), null);
    }

    @Override
    public void sendNonRecurringBillEndingReminder(UserVO userVO, String pkgDescription, Date endDate) {
        myLog.debug("sendNonRecurringBillEndingReminder " + userVO.getUserid() + "*" + endDate);
    }

    @Override
    public void sendThanksGifting(String market, String langCode, GiftPaymentLogVO giftPaymentLogVO) {
        myLog.debug("sendThanksGifting payer:" + giftPaymentLogVO.getMobileCountry().getCallingCode()
                + giftPaymentLogVO.getMobileNumber()
                + giftPaymentLogVO.getEmail()
                + " recipient: "
                + giftPaymentLogVO.getRecipientMobileCountry().getCallingCode()
                + giftPaymentLogVO.getRecipientMobileNumber()
                + giftPaymentLogVO.getRecipientEmail());

        TemplateBasedEmailVO buyerEmailVO = new TemplateBasedEmailVO(MessageType.THANKS_PURCHASE_GIFT,
                EmailRecipientVOFactory.createToGiftBuyer(giftPaymentLogVO), market, langCode);
        sendEmail(buyerEmailVO, null);

        TemplateBasedEmailVO recipientEmailVO = new TemplateBasedEmailVO(MessageType.THANKS_RECEIVE_GIFT,
                EmailRecipientVOFactory.createToGiftReceiver(giftPaymentLogVO), market, langCode);
        sendEmail(recipientEmailVO, null);

        // TODO: SMS to buyer?

        TemplateBasedSMSVO recipientSMSVO = new TemplateBasedSMSVO(MessageType.THANKS_RECEIVE_GIFT,
                SMSRecipientVOFactory.createToGiftReceiver(giftPaymentLogVO), market, langCode);
        sendSMS(recipientSMSVO, null, null);
    }

    @Override
    public void sendEventPushNotification(UserVO fromUserVO, UserVO toUserVO, PushNotificationEventType pushNotificationEventType, SecqMeEventVO eventVO) {
        myLog.debug("sendEventPushNotification sender: " + fromUserVO.getUserid() + ", receiver: " + toUserVO.getUserid() + ", type: " + pushNotificationEventType);
        MessageType messageType = null;
        EventType eventType = null;
        if (PushNotificationEventType.NORMAL_EVENT == pushNotificationEventType) {
            messageType = MessageType.PUSH_MESSAGE_NORMAL_EVENT;
            eventType = EventType.NORMAL;
        } else if (PushNotificationEventType.NORMAL_EVENT_END == pushNotificationEventType) {
            messageType = MessageType.PUSH_MESSAGE_NORMAL_EVENT_END;
            eventType = EventType.NORMAL_END;
        } else if (PushNotificationEventType.EMERGENCY_EVENT == pushNotificationEventType) {
            messageType = MessageType.PUSH_MESSAGE_EMERGENCY_EVENT;
            eventType = EventType.EMERGENCY;
        } else if (PushNotificationEventType.EMERGENCY_EVENT_END == pushNotificationEventType) {
            messageType = MessageType.PUSH_MESSAGE_EMERGENCY_EVENT_END;
            eventType = EventType.EMERGENCY_END;
        }

        TemplateBasedPushMessageVO pushMessageVO = new TemplateBasedPushMessageVO(messageType, fromUserVO, PushMessageRecipientVOFactory.createToContactAsUser(fromUserVO, toUserVO), fromUserVO.getMarketVO().getName(), fromUserVO.getLangCode());
        ParameterizableUtils.fillEventParams(pushMessageVO, eventVO);

        pushMessageVO.putPayload("name", fromUserVO.getAliasName());
        pushMessageVO.putPayload("action", "com.secqme.client.andrioid.UPDATE_STATUS");
        pushMessageVO.putPayload("eventId", String.valueOf(eventVO.getId()));
        pushMessageVO.putPayload("eventType", eventType.toString());

        sendPushMessage(pushMessageVO);
    }

    @Override
    public void notifyContactToUpdate(UserVO userVO, List<ContactVO> contactList) {
        myLog.debug("notifyContactToUpdate:" + userVO.getUserid() + "*" + contactList.size());

        for (final ContactVO contactVO : contactList) {
            if (contactVO.getEmailAddress() != null) {
                TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.NOTIFY_CONTACT_TO_UPDATE,
                        EmailRecipientVOFactory.createToContactVO(contactVO),
                        userVO.getMarketVO().getName(), userVO.getLangCode());
                this.sendEmail(emailVO, new EmailLogFiller(userVO, contactVO.getContactUserVO(), contactVO, null));
            }

            if (contactVO.getMobileNo() != null) {
                TemplateBasedSMSVO smsVO = new TemplateBasedSMSVO(MessageType.NOTIFY_CONTACT_TO_UPDATE,
                        SMSRecipientVOFactory.createToContactVO(contactVO),
                        userVO.getMarketVO().getName(), userVO.getLangCode());
                this.sendSMS(smsVO, new SMSLogFiller(userVO, contactVO.getContactUserVO(), contactVO, null), null);
            }
        }
    }

    @Override
    public void resendNotificationToContact(final UserVO userVO, final ContactVO contactVO) {
        myLog.debug("resendNotificationToContact:" + userVO.getUserid()
                + "*" + contactVO.getId());

        if (contactVO.getEmailAddress() != null) {
            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.RESEND_NOTIFICATION_TO_CONTACT,
                    EmailRecipientVOFactory.createToContactVO(contactVO),
                    userVO.getMarketVO().getName(), userVO.getLangCode());
            this.sendEmail(emailVO, new EmailLogFiller(userVO, contactVO.getContactUserVO(), contactVO, null));
        }

        if (contactVO.getMobileNo() != null) {
            TemplateBasedSMSVO smsVO = new TemplateBasedSMSVO(MessageType.RESEND_NOTIFICATION_TO_CONTACT,
                    SMSRecipientVOFactory.createToContactVO(contactVO),
                    userVO.getMarketVO().getName(), userVO.getLangCode());
            this.sendSMS(smsVO, new SMSLogFiller(userVO, contactVO.getContactUserVO(), contactVO, null), null);
        }
    }

    @Override
    public void sendTestHtmlEmailSms(String name, String email, CountryVO countryVO, String mobileNumber) {
        if (email != null && StringUtils.isNotEmpty(email)) {
            String recipientEmailText = "Content based email";
            String recipientEmailSubject = "Content based email";

            EmailRecipientVO emailRecipientVO = new EmailRecipientVO(name, email);
            emailRecipientVO.putParam("TEST_NAME", name);
            emailRecipientVO.putParam("TEST_EMAIL", email);

            ContentBasedEmailVO contentBasedEmailVO = new ContentBasedEmailVO(MessageType.TEST, emailRecipientVO, recipientEmailSubject, recipientEmailText);
            myLog.debug("Test ContentBasedEmailVO: " + contentBasedEmailVO);
            sendEmail(contentBasedEmailVO, null);

            TemplateBasedEmailVO templateBasedEmailVO = new TemplateBasedEmailVO(MessageType.TEST, emailRecipientVO, "default", "en_US");
            myLog.debug("Test TemplateBasedEmailVO: " + templateBasedEmailVO);
            sendEmail(templateBasedEmailVO, null);
        }

        if (countryVO != null && StringUtils.isNotEmpty(mobileNumber)) {
            String recipientSMSText = "Content based SMS";

            SMSRecipientVO smsRecipientVO = new SMSRecipientVO(countryVO, mobileNumber);
            smsRecipientVO.putParam("TEST_NAME", name);
            smsRecipientVO.putParam("TEST_EMAIL", email);

            ContentBasedSMSVO contentBasedSMSVO = new ContentBasedSMSVO(MessageType.TEST, smsRecipientVO, recipientSMSText);
            myLog.debug("Test ContentBasedSMSVO: " + contentBasedSMSVO);
            sendSMS(contentBasedSMSVO, null, null);

            TemplateBasedSMSVO templateBasedSMSVO = new TemplateBasedSMSVO(MessageType.TEST, smsRecipientVO, "default", "en_US");
            myLog.debug("Test TemplateBasedSMSVO: " + templateBasedSMSVO);
            sendSMS(templateBasedSMSVO, null, null);
        }
    }

    @Override
    public void testSNS(UserVO userVO, String message) {
        if (message == null) {
            message = "test SNS";
            //todo custom template for test SNS message
//    		Map msgModel = prepareNotificationMessage(event);
//    		String snsText = VelocityEngineUtils.mergeTemplateIntoString(
//                vEngine, loadTemplateFromCache(SNS_UPDATE_VM, event.getUserVO().getMarketVO().getName()), "utf-8", msgModel);
        }
        boolean requireNotified = false;
        if (userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
            for (UserSNSConfigVO userSNSConfigVO : userVO.getSnsConfigList()) {
                if (userSNSConfigVO.isNotify()) {
                    requireNotified = true;
                    break;
                }
            }
        }

        if (requireNotified) {
            TemplateBasedSNSMessageVO snsMessageVO = new TemplateBasedSNSMessageVO(MessageType.TEST, userVO, userVO.getMarketVO().getName(), userVO.getLangCode());
            publishSNSMessage(snsMessageVO);
        }
    }

    public void addMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns) {
        FutureTask futureTask = new FutureTask(
                new MarketingEmailSubscribeWorker(marketingEmailService, userVO, campaigns));
        threadPool.submit(futureTask);
    }

    public void addMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns) {
        FutureTask futureTask = new FutureTask(
                new MarketingEmailSubscribeWorker(marketingEmailService, contactVO, campaigns));
        threadPool.submit(futureTask);
    }


    public void deleteMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns) {
        FutureTask futureTask = new FutureTask(
                new MarketingEmailUnsubscribeWorker(marketingEmailService, userVO, campaigns));
        threadPool.submit(futureTask);
    }

    public void deleteMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns) {
        FutureTask futureTask = new FutureTask(
                new MarketingEmailUnsubscribeWorker(marketingEmailService, contactVO, campaigns));
        threadPool.submit(futureTask);
    }

    public static class EmailLogFiller implements EmailService.OnBeforeEmailLogSavedListener {

        public UserVO requester;
        public UserVO recipient;
        public ContactVO relatedContactVO;
        public SecqMeEventVO relatedEventVO;

        public EmailLogFiller(UserVO requester, UserVO recipient, ContactVO relatedContactVO, SecqMeEventVO relatedEventVO) {
            this.requester = requester;
            this.recipient = recipient;
            this.relatedContactVO = relatedContactVO;
            this.relatedEventVO = relatedEventVO;
        }

        @Override
        public void onBeforeEmailLogSaved(EmailVO emailVO, EmailRecipientVO emailRecipientVO, EmailLogVO log) {
            log.setRequesterUser(requester);
            log.setRecipientUser(recipient);
            if (relatedContactVO != null) log.setContactId(relatedContactVO.getId());
            if (relatedEventVO != null) log.setEventId(relatedEventVO.getId());
        }
    }

    public static class SMSLogFiller implements SMSManager.OnBeforeSMSLogSavedListener {

        public UserVO requester;
        public UserVO recipient;
        public ContactVO relatedContactVO;
        public SecqMeEventVO relatedEventVO;

        public SMSLogFiller(UserVO requester, UserVO recipient, ContactVO relatedContactVO, SecqMeEventVO relatedEventVO) {
            this.requester = requester;
            this.recipient = recipient;
            this.relatedContactVO = relatedContactVO;
            this.relatedEventVO = relatedEventVO;
        }

        @Override
        public void onBeforeSMSLogSaved(SMSVO smsVO, SMSRecipientVO smsRecipientVO, SMSLogVO log) {
            // log.setRequesterUser(requester);
            // log.setRecipientUser(recipient);
            if (relatedContactVO != null) log.setContactId(relatedContactVO.getId());
            if (relatedEventVO != null) log.setEventId(relatedEventVO.getId());
        }
    }

    public class SendMissedConfigureContact implements SMSManager.OnAfterSMSLogSavedListener {

        private ContactVO contactVO;

        public SendMissedConfigureContact(ContactVO contactVO) {
            this.contactVO = contactVO;
        }

        @Override
        public void onAfterSMSLogSaved(SMSVO smsVO, SMSRecipientVO smsRecipientVO, SMSLogVO log) {
            if (!log.isSendOut()) {
                DefaultNotificationEngine.this.sendContactMisconfiguredEmail(contactVO);
            }
        }
    }
}

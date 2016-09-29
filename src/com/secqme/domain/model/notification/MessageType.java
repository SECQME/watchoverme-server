package com.secqme.domain.model.notification;

/**
 *
 * @author jameskhoo
 */
public enum MessageType {

    MARKETING_CAMPAIGN(null),
    EMAIL_VERIFICATION("EmailVerification"),
    EMAIL_CHANGED_VERIFICATION("EmailChangedVerification"),
    PASSWORD_RESET("PasswordReset"),
    PASSWORD_RESET_VERIFICATION_PIN("PasswordResetVerificationPin"),
    PASSWORD_RESET_TOKEN_LINK("PasswordResetTokenLink"),
    CONTACT_INVITATION("ContactAdded"),
    CONTACT_INVITATION_REMINDER("ContactInvitationReminder"),
    CONTACT_MOBILE_MISCONFIGURED("contactMisConfigured"),
    REJECTED_EMERGENCY_CONTACT("RejectedEmergencyContact"),
    LEFT_EMERGENCY_CONTACT("LeftEmergencyContact"),
    EMERGENCY_NOTIFICATION("EmergencyNotification"),
    EVENT_EXPIRE_NOTIFICATION("ExpiredEventNotification"),
    SAFETY_ARRIVAL_NOTIFICATION("SafetyArrivalNotification"),
    SHARE_EVENT_EMAIL_AND_SMS("ShareEventEmailAndSms"),
    SHARE_EVENT_END_EMAIL_AND_SMS("ShareEventEndEmailAndSms"),
    MOBILE_NO_VERIFICATION("mobileNoVerificationSMS"),
    THANKS_PURCHASE_GIFT("ThanksPurchaseGift"),
    THANKS_RECEIVE_GIFT("ThanksReceiveGift"),
    PUSH_MESSAGE_NORMAL_EVENT("PushMessageNormalEvent"),
    PUSH_MESSAGE_NORMAL_EVENT_END("PushMessageNormalEventEnd"),
    PUSH_MESSAGE_EMERGENCY_EVENT("PushMessageEmergencyEvent"),
    PUSH_MESSAGE_EMERGENCY_EVENT_END("PushMessageEmergencyEventEnd"),
    WATCH_OVER_ME_NOW("WatchOverMeNow"),
    WELCOME_LITE_PACKAGE("WelcomeLitePackage"),
    WELCOME_PREMIUM_PACKAGE("WelcomePremiumPackage"),

    // What is this?
    TRIAL_EVENT_NOTIFICATION("TrialEventSMS"),
    NOTIFY_CONTACT_TO_UPDATE("NotifyContactToUpdate"),
    RESEND_NOTIFICATION_TO_CONTACT("ResendNotificationToContact"),

    TEST("Test"),

    @Deprecated // Not Used?
    REFERRAL("Referral"),
    @Deprecated // Use getresponse.com
    THANKS_SUBSCRIPTION("ThanksSubscribe");

    private String defaultTemplateCode;

    MessageType(String tCode) {
        this.defaultTemplateCode = tCode;
    }

    public String getDefaultTemplateCode() {
        return defaultTemplateCode;
    }

    public void setDefaultTemplateCode(String defaultTemplateCode) {
        this.defaultTemplateCode = defaultTemplateCode;
    }
}



package com.secqme.util.spring;

/**
 *
 * @author jameskhoo
 */
public enum BeanType {
    entityManagerFactory("entityManagerFactory"),

    AR_MARKET_MESSAGE_TEMPLATE_DAO("arMarketMessageTemplateDAO"),
    BILLING_CYCLE_DAO("billingCycleDAO"),
    BILLING_LOG_DAO("billingLogDAO"),
    BILLING_PKG_DAO("billingPkgDAO"),
    CAMPAIGN_DAO("campaignDAO"),
    CONTACT_DAO("contactDAO"),
    CRIME_REPORT_DAO("crimeReportDAO"),
    EMAIL_LOG_DAO("emailLogDAO"),
    EVENT_LOG_DAO("eventLogDAO"),
    GIFT_PAYMENT_LOG_DAO("giftPaymentLogDAO"),
    GOOGLE_GEO_CODE_CACHE_DAO("googleGeoCodeCacheDAO"),
    GOOGLE_PAYMENT_LOG_DAO("googlePaymentLogDAO"),
    GOOGLE_PLACE_CACHE_DAO("googlePlaceCacheDAO"),
    ITUNE_PAYMENT_LOG_DAO("iTunePaymentLogDAO"),
    LANGUAGE_DAO("languageDAO"),
    MARKET_DAO("marketDAO"),
    MARKETING_TEXT_CONFIG_DAO("marketingTextConfigDAO"),
    PAYMENT_CLICK_DAO("paymentClickDAO"),
    PAYMENT_GW_DAO("paymentGWDAO"),
    PAYMENT_HISTORY_DAO("paymentHistoryDAO"),
    PAYPAL_IPN_LOG_DAO("paypalIPNLogDAO"),
    PENDING_PAYMENT_DAO("pendingPaymentDAO"),
    PRICING_PACKAGE_DAO("pricingPackageDAO"),
    PROMO_CODE_DAO("promoCodeDAO"),
    PROMO_CODE_LOG_DAO("promoCodeLogDAO"),
    PROMOTION_DAO("promotionDAO"),
    PUSH_MESSAGE_LOG_DAO("pushMessageLogDAO"),
    QUICK_EVENT_TEMPLATE_DAO("quickEventTemplateDAO"),
    REFERRAL_LOG_DAO("referralLogDAO"),
    REFERRAL_PROGRAM_DAO("referralProgramDAO"),
    REFERRAL_PROGRAM_MARKETING_TEXT_DAO("referralProgramMarketingTextDAO"),
    REFERRAL_REPORT_DAO("referralReportDAO"),
    SAVED_EVENTS_DAO("savedEventsDAO"),
    SECQME_EVENT_DAO("secqmeEventDAO"),
    SERVER_PARAM_DAO("serverParamDAO"),
    SHARE_EVENT_DAO("shareEventDAO"),
    SMS_GW_DAO("smsGWDAO"),
    SMS_LOG_DAO("smsLogDAO"),
    SNS_LOG_DAO("snsLogDAO"),
    SOCIAL_NETWORK_DAO("socialNetworkDAO"),
    SUBSCRIPTION_INFO_DAO("subscriptionInfoDAO"),
    SUBSCRIPTION_INFO_LOG_DAO("subscriptionInfoLogDAO"),
    TIME_ZONE_DAO("timeZoneDAO"),
    TRACKING_LOG_DAO("trackingLogDAO"),
    USER_DAO("userDAO"),
    USER_SNS_CONFIG_DAO("userSNSConfigDAO"),
    USER_TRACKING_LOG("userTrackingLogDAO"),

    userManager("myUserManager"),
    eventManager("myEventManager"),
    textUtil("textUtil"),
    cacheUtil("cacheUtil"),
    restUtil("restUtil"),
    smsManager("smsManager"),
    SNS_MANAGER("snsManager"),
    emailService("myEmailService"),
    scheduleFactory("scheduleFactory"),
    timerFactory("timerFactory"),
    paypalUtil("paypalUtil"),
    serverStatusUtil("serverStatusUtil"),
    faceBookUtil("myFacebookUtil"),
    billingManager("myBillingManager"),
    twitterUtil("myTwitterUtil"),
    sysAdminUtil("systemAdminUtil"),
    paymentManager("myPaymentManager"),
    trackingManager("myTrackingManager"),
    applePaymentUtil("applePaymentUtil"),
    googlePaymentUtil("googlePaymentUtil"),
    arTemplateEngine("arTemplateEngine"),
    scheduleManager("scheduleManager"),
    locationUtil("locationUtil"),
    promotionManager("promotionManager"),
    quickEventManager("quickEventManager"),
    localeManager("localeManager"),
    safeZoneManager("safeZoneManager"),
    referralManager("referralManager"),
    crimeManager("crimeManager"),
    userSubscriptionInfoManager("userSubscriptionInfoManager"),
    mobileNumberUtil("mobileNumberUtil"),
    notificationEngine("notificationEngine"),

    CONTACT_MANAGER("contactManager"),
    EMAIL_LOG_MANAGER("emailLogManager"),
    PROMO_CODE_MANAGER("promoCodeManager"),

    PARSE_PUSH_SERVICE("parsePushService"),
    FACEBOOK_SNS_SERVICE("fbSNService");

    private String beanName = null;

    BeanType(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return this.beanName;
    }

}

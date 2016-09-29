package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.dao.*;
import com.secqme.domain.model.CountryVO;
import com.secqme.manager.promotion.PromotionManager;
import com.secqme.sns.FacebookUtil;
import com.secqme.util.LocaleManager;
import com.secqme.util.LocationUtil;
import com.secqme.util.PasswordGenerator;
import com.secqme.util.io.MediaFileManager;
import com.secqme.util.TextUtil;
import com.secqme.util.cache.CacheUtil;
import com.secqme.util.notification.NotificationEngine;
import com.secqme.util.notification.sms.SMSManager;
import com.secqme.util.notification.sns.SNSManager;
import com.secqme.util.validator.EmailAddressUtil;
import com.secqme.util.validator.MobileNumberUtil;
import com.secqme.util.schedular.ScheduleManager;
import com.secqme.util.security.SecurityHelper;
import com.secqme.util.shorturl.UrlShortenerService;

import javax.validation.Validator;

/**
 *
 * @author james
 */
public class BaseManager {

    private CampaignsDAO campaignsDAO = null;
    private ContactDAO contactDAO = null;
    private CrimeReportDAO crimeReportDAO = null;
    private EmailLogDAO emailLogDAO = null;
    private EventLogDAO eventLogDAO = null;
    private ReferralLogDAO referralLogDAO = null;
    private ReferralProgramDAO referralProgramDAO = null;
    private ReferralProgramMarketingTextDAO referralProgramMarketingTextDAO = null;
    private ReferralReportDAO referralReportDAO = null;
    private SavedEventsDAO savedEventsDAO = null;
    private SecqMeEventDAO secqMeEventDAO = null;
    private ServerParamDAO serverParamDAO = null;
    private ShareEventDAO shareEventDAO = null;
    private SMSLogDAO smsLogDAO = null;
    private TrackingLogDAO trackingLogDAO = null;
    private UserDAO userDAO = null;
    private UserSNSConfigDAO userSNSConfigDAO = null;

    private LocaleManager localeManager = null;
    private MediaFileManager mediaFileManager = null;
    private PromotionManager promotionManager = null;
    private ScheduleManager scheduleManager = null;
    private SMSManager smsManager = null;
    private SNSManager snsManager = null;

    private CacheUtil cacheUtil = null;
    private EmailAddressUtil emailAddressUtil = null;
    private FacebookUtil fbUtil = null;
    private LocationUtil locationUtil = null;
    private MobileNumberUtil mobileNumberUtil = null;
    private NotificationEngine notificationEngine = null;
    private PasswordGenerator pronounceablePasswordGenerator = null;
    private SecurityHelper securityHelper = null;
    private TextUtil textUtil = null;
    private UrlShortenerService urlShortenerService = null;
    private Validator validator = null;

    public CampaignsDAO getCampaignsDAO() {
        return campaignsDAO;
    }

    public void setCampaignsDAO(CampaignsDAO campaignsDAO) {
        this.campaignsDAO = campaignsDAO;
    }

    public ContactDAO getContactDAO() {
        return contactDAO;
    }

    public void setContactDAO(ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }

    public CrimeReportDAO getCrimeReportDAO() {
        return crimeReportDAO;
    }

    public void setCrimeReportDAO(CrimeReportDAO crimeReportDAO) {
        this.crimeReportDAO = crimeReportDAO;
    }

    public EmailLogDAO getEmailLogDAO() {
        return emailLogDAO;
    }

    public void setEmailLogDAO(EmailLogDAO emailLogDAO) {
        this.emailLogDAO = emailLogDAO;
    }

    public EventLogDAO getEventLogDAO() {
        return eventLogDAO;
    }

    public void setEventLogDAO(EventLogDAO eventLogDAO) {
        this.eventLogDAO = eventLogDAO;
    }

    public ReferralLogDAO getReferralLogDAO() {
        return referralLogDAO;
    }

    public void setReferralLogDAO(ReferralLogDAO referralLogDAO) {
        this.referralLogDAO = referralLogDAO;
    }

    public ReferralProgramDAO getReferralProgramDAO() {
        return referralProgramDAO;
    }

    public void setReferralProgramDAO(ReferralProgramDAO referralProgramDAO) {
        this.referralProgramDAO = referralProgramDAO;
    }

    public ReferralProgramMarketingTextDAO getReferralProgramMarketingTextDAO() {
        return referralProgramMarketingTextDAO;
    }

    public void setReferralProgramMarketingTextDAO(ReferralProgramMarketingTextDAO referralProgramMarketingTextDAO) {
        this.referralProgramMarketingTextDAO = referralProgramMarketingTextDAO;
    }

    public ReferralReportDAO getReferralReportDAO() {
        return referralReportDAO;
    }

    public void setReferralReportDAO(ReferralReportDAO referralReportDAO) {
        this.referralReportDAO = referralReportDAO;
    }

    public SavedEventsDAO getSavedEventsDAO() {
        return savedEventsDAO;
    }

    public void setSavedEventsDAO(SavedEventsDAO savedEventsDAO) {
        this.savedEventsDAO = savedEventsDAO;
    }

    public SecqMeEventDAO getSecqMeEventDAO() {
        return secqMeEventDAO;
    }

    public void setSecqMeEventDAO(SecqMeEventDAO secqMeEventDAO) {
        this.secqMeEventDAO = secqMeEventDAO;
    }

    public ServerParamDAO getServerParamDAO() {
        return serverParamDAO;
    }

    public void setServerParamDAO(ServerParamDAO serverParamDAO) {
        this.serverParamDAO = serverParamDAO;
    }

    public ShareEventDAO getShareEventDAO() {
        return shareEventDAO;
    }

    public void setShareEventDAO(ShareEventDAO shareEventDAO) {
        this.shareEventDAO = shareEventDAO;
    }

    public SMSLogDAO getSmsLogDAO() {
        return smsLogDAO;
    }

    public void setSmsLogDAO(SMSLogDAO smsLogDAO) {
        this.smsLogDAO = smsLogDAO;
    }

    public TrackingLogDAO getTrackingLogDAO() {
        return trackingLogDAO;
    }

    public void setTrackingLogDAO(TrackingLogDAO trackingLogDAO) {
        this.trackingLogDAO = trackingLogDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserSNSConfigDAO getUserSNSConfigDAO() {
        return userSNSConfigDAO;
    }

    public void setUserSNSConfigDAO(UserSNSConfigDAO userSNSConfigDAO) {
        this.userSNSConfigDAO = userSNSConfigDAO;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public void setLocaleManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    public MediaFileManager getMediaFileManager() {
        return mediaFileManager;
    }

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    public PromotionManager getPromotionManager() {
        return promotionManager;
    }

    public void setPromotionManager(PromotionManager promotionManager) {
        this.promotionManager = promotionManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public void setScheduleManager(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    public SMSManager getSmsManager() {
        return smsManager;
    }

    public void setSmsManager(SMSManager smsManager) {
        this.smsManager = smsManager;
    }

    public SNSManager getSnsManager() {
        return snsManager;
    }

    public void setSnsManager(SNSManager snsManager) {
        this.snsManager = snsManager;
    }

    public CacheUtil getCacheUtil() {
        return cacheUtil;
    }

    public void setCacheUtil(CacheUtil cacheUtil) {
        this.cacheUtil = cacheUtil;
    }

    public EmailAddressUtil getEmailAddressUtil() {
        return emailAddressUtil;
    }

    public void setEmailAddressUtil(EmailAddressUtil emailAddressUtil) {
        this.emailAddressUtil = emailAddressUtil;
    }

    public FacebookUtil getFbUtil() {
        return fbUtil;
    }

    public void setFbUtil(FacebookUtil fbUtil) {
        this.fbUtil = fbUtil;
    }

    public LocationUtil getLocationUtil() {
        return locationUtil;
    }

    public void setLocationUtil(LocationUtil locationUtil) {
        this.locationUtil = locationUtil;
    }

    public MobileNumberUtil getMobileNumberUtil() {
        return mobileNumberUtil;
    }

    public void setMobileNumberUtil(MobileNumberUtil mobileNumberUtil) {
        this.mobileNumberUtil = mobileNumberUtil;
    }

    public NotificationEngine getNotificationEngine() {
        return notificationEngine;
    }

    public void setNotificationEngine(NotificationEngine notificationEngine) {
        this.notificationEngine = notificationEngine;
    }

    public PasswordGenerator getPronounceablePasswordGenerator() {
        return pronounceablePasswordGenerator;
    }

    public void setPronounceablePasswordGenerator(PasswordGenerator pronounceablePasswordGenerator) {
        this.pronounceablePasswordGenerator = pronounceablePasswordGenerator;
    }

    public SecurityHelper getSecurityHelper() {
        return securityHelper;
    }

    public void setSecurityHelper(SecurityHelper securityHelper) {
        this.securityHelper = securityHelper;
    }

    public TextUtil getTextUtil() {
        return textUtil;
    }

    public void setTextUtil(TextUtil textUtil) {
        this.textUtil = textUtil;
    }

    public UrlShortenerService getUrlShortenerService() {
        return urlShortenerService;
    }

    public void setUrlShortenerService(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    protected CountryVO validateUserMobileNumber(String mobileCountryISO, String mobileNumber, String langCode) throws CoreException {
        CountryVO mobileCountry = getSmsManager().getCountry(mobileCountryISO);
        if (mobileCountry == null) {
            throw new CoreException(ErrorType.USER_COUNTRY_NOT_SUPPORTED_ERROR, langCode, mobileCountryISO);
        }

        // try to validate user mobile number here.
        //

        String i18nMobileNumber = "+" + mobileCountry.getCallingCode() +
                mobileNumber;
        if (!getMobileNumberUtil().isValidMobileNumber(i18nMobileNumber, mobileCountryISO)) {
            throw new CoreException(ErrorType.USER_MOBILE_NUMBER_INVALID, langCode, i18nMobileNumber);
        }
        return mobileCountry;
    }
}

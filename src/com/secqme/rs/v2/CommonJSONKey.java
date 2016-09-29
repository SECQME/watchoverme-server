package com.secqme.rs.v2;

/**
 * User: James Khoo
 * Date: 2/8/14
 * Time: 11:37 AM
 */
public interface CommonJSONKey {

    public final static String CONTACT_LIST_KEY = "contactList";
    public final static String CONTACT_EVENT_LIST_KEY = "contactEventList";
    public final static String CONTACT_ID_LIST_KEY = "contactIdList";
    public final static String SNS_LIST_KEY  = "snsList";
    public final static String COUNTRY_LIST_KET = "countryList";
    public final static String SERVER_VERSION_KEY = "serverVersion";
    public final static String CLIENT_VERSION_KEY = "clientVersion";
    public final static String CLIENT_OS_KEY = "clientOS";
    public final static String NOTIFY_FACEBOOK_KEY = "facebook";
    public final static String OK_RESULT_WITH_DURATION = "{\"status\":\"extend\", \"duration\":%1$d}"; //khlow20120607

    public final static String TIME_STAMP_KEY = "timeStamp";


    // User Account DATA
    public final static String VALID_ACCT_KEY = "validAcct";
    public final static String EMAIL_ADDR_KEY = "emailAddr";
    public final static String MOBILE_NO_KEY = "mobileNumber";
    public static final String MOBILE_COUNTRY_KEY = "mobileCountry";
    public static final String I18N_MOBILE_NUMBER_KEY = "i18nMobileNumber";
    public static final String MOBILE_COUNTRY_CODE_KEY = "mobileCountryCode";
    public static final String RELATIONSHIP_KEY = "relationship";
    public final static String MOBILE_VERIFY_PIN_KEY = "mobileVerifyPin";
    public final static String AUTH_TOKEN_KEY = "authToken";
    public final static String USER_ID_KEY = "userid";
    public final static String USER_NICK_NAME_KEY = "nickName";
    public static final String USER_NAME_KEY = "name";
    public final static String USER_PASSWORD_KEY = "password";
    public static final String USER_NEW_PASSWORD_KEY = "newPassword";
    public final static String DEVICE_KEY = "device";
    public static final String PASSWORD_RESET_PIN_KEY = "passwordResetPin";
    public static final String CHECK_MOBILE = "checkMobile";
    public static final String PLACE_NAME_KEY = "placeName";

    public final static String USER_LATEST_EVENT_KEY = "userLatestEvent";
    public final static String USER_SNS_CONFIG_KEY = "userSNSConfig";
    public final static String USER_SNS_CONFIGS_KEY = "userSNSConfigs";
    public final static String USER_IP_ADDRESS_KEY = "ipAddress"; //khlow20120621
    public final static String SNS_ACCESS_TOKEN = "snsAccessToken";
    public final static String USER_COUNTRY_KEY = "country";
    public final static String SNS_NAME = "snsName";
    public final static String LANG_CODE_KEY = "langCode";
    public final static String STATUS = "status";
    public final static String MESSAGE_TOKEN_TYPE_KEY = "messageTokenType";
    public final static String PUSH_MESSAGE_TOKEN_KEY = "pushMessageToken";
    public final static String IOS_LOWEST_MONTHLY_PRICE = "iosLowestMonthlyPrice";
    public final static String SNS_ID = "snsId";
    public static final String FACEBOOK_CONNECTED_KEY = "facebookConnected";
    public static final String FACEBOOK_NOTIFY_KEY = "facebookNotify";
    public static final String NETWORK_CONTACT_KEY = "networkContact";
    public static final String NOTIFY_CONTACT_KEY = "notifyContact";
    public static final String PROFILE_PICTURE_URL_KEY = "profilePictureURL";
    // SafeZone
    public static final String ZONE_ID_KEY = "zoneId";
    public static final String CREATED_BY_USER_KEY = "createdBy";
    public static final String ZONE_NAME_KEY = "zoneName";
    public static final String ZONE_ADDRESS_KEY = "zoneAddress";
    public static final String SAFE_ZONE_LIST_KEY = "safeZoneList";
    public static final String AUTO_CONFIRM_SAFETY_KEY = "autoConfirmSafety";
    public static final String SAFE_ZONE_ID_LIST_KEY = "safeZoneIdList";
    // Event Data
    public static final String TOTAL_EVENT_REGISTERED_KEY = "totalEventRegistered";
    public static final String EVENT_HISTORY_LIST_KEY = "eventList";
    public static final String CONFIRM_SAFETY = "confirmSafety";
    public static final String CONFIRM_SAFETY_TIME = "confirmSafetyTime";
    public static final String EVENT_END_TIME_KEY = "eventEndTime";
    public final static String LOCATION_KEY = "location";
    public final static String LATITUDE_KEY = "latitude";
    public final static String LONGITUDE_KEY = "longitude";
    public final static String LOCATION_ACCURACY_KEY = "accuracy";
    public final static String EVENT_TYPE_KEY = "eventType";
    public final static String EVENT_STATUS_KEY = "eventStatus";
    public final static String EVENT_TRACKING_URL = "eventTrackingURL";
    public final static String TIME_ZONE_KEY = "timeZone";

    public final static String TRACKING_PIN_KEY = "trackingPin";
    public final static String EVENT_DURATION_KEY = "eventDuration";
    public final static String EVENT_START_TIME_KEY = "eventStartTime";
    public final static String NEW_EVENT_START_TIME_KEY = "newEventStartTime";
    public final static String TRIGGER_NOTIFICATION_KEY = "triggerNotification";
    public static final String EMERGENCY_PICTURE_KEY = "emergencyPicture";
    public static final String EMERGENCY_VIDEO_LIST_KEY = "emergencyVideos";
    public final static String EVENT_PIN_KEY = "eventPin";
    public final static String EVENT_MESSAGE_KEY = "message";
    public final static String AUDIO_STREAM_KEY = "audioStream";
    public final static String PICTURE_STREAM_KEY = "pictureStream";
    public final static String VIDEO_STREAM_KEY = "videoStream";
    public final static String DURATION_KEY = "duration"; // durationInMinute
    public final static String DURATION_IN_SECOND_KEY = "durationInSecond"; // durationInSecond
    public final static String SAVED_EVENTS_KEY = "savedEvents";

    public static final String PRICE_PKG_CODE = "pricePkgCode";
    public static final String GOOGLE_IN_APP_RECEIPT = "googleInAppReceipt";

    // Specific Key for CrimeZoneData
    public static final String EVENT_ID_KEY = "eventId";
    public static final String CRIME_NOTE_KEY = "note";
    public static final String CRIME_TYPE_KEY  = "crimeType";
    public static final String CRIME_REPORT_KEY = "crimeReport";
    public static final String CRIME_REPORT_TIME_KEY = "crimeReportTime";
    public static final String CRIME_PICTURE_KEY = "crimePic";
    public static final String CRIME_TIME_RANGE_KEY = "timeRange";
    public static final String CRIME_PIC_URL_KEY = "crimePicURL";
    public static final String CRIME_TIME_ZONE_KEY = "crimeTimeZone";
    public static final String CRIME_REPORT_LIST_KEY = "crimeReportLIST";


    // Google Place
    public static final String GOOGLE_PLACE_ARRAY_KEY = "googlePlaces";

    // User SNS Config
    public static final String SNS_NAME_KEY =  "snsName";
    public static final String SNS_UID_KEY = "snsUID";
    public static final String SNS_ADDITONAL_CONFIG_KEY = "snsAdditionalConfig";

    // CrimeZone List
    public static final String CRIME_ZONE_LIST_KEY = "crimeZoneList";
    public static final String CRIME_ZONE_COUNTRY_CODE_KEY = "crimeZoneCountry";
    public static final String CRIME_ZOME_CITY_KEY = "crimeZoneCity";
    public static final String CRIME_ZONE_RADIUS_KEY = "crimeZoneRadius";
    public static final String CRIME_ZONE_NAME_KEY = "crimeZoneName";
    public static final String CRIME_ZONE_MESSAGE_KEY  = "crimeZoneMessage";
    public static final String CRIME_ZONE_DISTANCE_KEY = "crimeZoneDistanceInKM";


    // User Billing Data
    public final static String SUBSCRIPTION_PKG_KEY = "subscription";
    public final static String BILLING_PKG_KEY = "billingPackages";
    public final static String ONE_TIME_BILLING_PKG_KEY = "oneTimePackages";
    public final static String MAX_CONTACT_ALLOWS = "maxContactAllows";
    public final static String USER_CURRENT_BALANCE_KEY = "userCurrentBalance";
    public final static String TRIAL_PACKAGE_ACTIVATED_KEY = "trialPkgActivated";
    public static final String TRIAL_PACKAGE_MARKETING_TEXT_KEY = "trialPkgMarketingText";
    public final static String TRIAL_PACKAGE_ACTIVATED_AT = "trialPkgActivatedAt";
    public static final String BILL_PKG_TYPE_KEY = "billPkgType";
    public static final String BILL_PKG_EFFECTIVE_DAYS_KEY = "billPkgDayRemaining";
    public static final String PREMIUM_PKG_CONFIG = "premiumBillPkgConfig";

    // Referral
    public static final String USER_REFERRAL_URL_KEY = "referralURL";
    public static final String REFERRAL_MARKETING_TEXT_KEY = "marketingText";
    public static final String REFERRAL_CLICK_COUNT_KEY = "clickCount";
    public static final String REFERRAL_INSTALL_COUNT_KEY = "installCount";
    public static final String REFERRAL_REGISTER_COUNT_KEY = "registerCount";
    public static final String REFERRAL_FRIEND_NAME_KEY = "friendName";
    public static final String REFERRAL_AWARDED_DURATION_KEY ="rewardInDays";
    public static final String REFERRAL_LIST_KEY = "referralList";
    public static final String REFERRAL_INFO_KEY = "referralInfo";

    // Payment
    public static final String APPLE_VERIFIED_RECEIPT_KEY = "appleVerifiedReceipt";
    public static final String PACKAGE_CODE = "packageCode";
    
    //user movement
    public static final String BACKGROUND_KEY = "background";
    
    // Safety level
    public static final String SAFETY_LEVEL_KEY = "safetyLevel";
    public static final String SAFETY_LEVEL_PERCENTAGE_KEY = "safetyLevelPercentage";
    public static final String SAFETY_LEVEL_LIST_KEY = "safetyLevelList";
    public static final String SAFETY_LEVEL_NAME_KEY = "safetyLevelName";
    public static final String SAFETY_LEVEL_DESCRIPTION_KEY = "safetyLevelDescription";
    public static final String SAFETY_LEVEL_ACTION_KEY = "safetyLevelAction";
    public static final String SAFETY_LEVEL_WEIGHT_KEY = "safetyLevelWeight";
    public static final String SAFETY_LEVEL_ENABLE_KEY = "safetyLevelEnable";

    public static final String MONTH = "month";
    public static final String ANALYTICS_IDS_KEY = "analyticsIds";
    public static final String ANALYTICS_ID_KEY = "analyticsId";
    public static final String PROMO_CODE_KEY = "promoCode";
    public static final String ID_KEY = "id";
    public static final String EMAIL_VERIFIED_AT = "emailVerifiedAt";
    public static final String NOTIFY = "notify";
    public static final String USER_ID_TYPE_KEY = "userIdType";
}

package com.secqme.util.cache;

/**
 * Default Cache Util for 
 * storing all systems configurations read from database
 * To avoid round trip to server;
 * Allows a cache Key to expire, thus 
 * force the configuration to reload;
 *
 * @author jameskhoo
 */
public interface CacheUtil {
    
    public static final String SMS_SERVICE_HASH_MAP_KEY = "smsServiceHashMap";
    public static final String SMS_COUNTRY_HASH_MAP_KEY = "smsCountryHashKey";
    public static final String SMS_COUNTRY_LIST_KEY = "smsCountryList";
    
    public static final String MARKET_MAP_KEY = "billingMarketMap";
    public static final String DEFAULT_BILLING_PKG_KEY = "billingDefaultPkg";
    public static final String DEFAULT_FREEMIUM_PKG_KEY = "billingDefaultFreemiumPkg";
    public static final String DEFAULT_TRIAL_PKG_KEY = "billingDefaultTrialPkg";
    public static final String DEFAULT_PREMIUM_PKG_KEY = "billingDefaultPremiumPkg";
    
    public static final String PAYMENT_GW_HASH_MAP_KEY = "paymentGWHashMap";
    public static final String QUICK_EVENT_TEMPLATE_LIST_KEY = "quickEventTemplateList";
    public static final String PRICING_PKG_KEY = "pricingPkg_";
    public static final String PROMOTION_KEY = "promotion_";
    
    public static final String TEMPLATE_KEY = "template_"; //khlow20120611
    
    public Object getCachedObject(String key);
    public void storeObjectIntoCache(String key, Object obj);
    public void expireCachedObject(String key);
    public void expireAllCacheObject();

}

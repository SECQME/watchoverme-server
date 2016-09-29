package com.secqme.util.shorturl;

import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author jameskhoo
 */
public class BitlyUrlShortenerService implements UrlShortenerService {

    private static String BIT_LY_URL_PREFIX = null;
    private RestUtil restUtil = null;
    private static CacheManager cacheManager = null;
    private static Cache shortURLCache = null;
    private static final Logger myLog = Logger.getLogger(BitlyUrlShortenerService.class);
    private final static String CACHE_NAME = "bitURLCache";
    private static int MAX_CACHE_OBJECT = 500;
    private static int CACHE_OBJ_EXPIRE_IN_SECOND = 86400;

    public BitlyUrlShortenerService(String loginName, String apiKey, RestUtil restUtil) {
        BIT_LY_URL_PREFIX = "http://api.bit.ly/v3/shorten?login=" + loginName
                + "&apiKey=" + apiKey
                + "&longUrl=";
        this.restUtil = restUtil;
        cacheManager = CacheManager.create();
        shortURLCache = new Cache(CACHE_NAME,
                MAX_CACHE_OBJECT,
                false,
                false,
                CACHE_OBJ_EXPIRE_IN_SECOND,
                CACHE_OBJ_EXPIRE_IN_SECOND);
    }

    public String getShortURL(String longURL, String feature) throws ShortURLException {
        String shortURL = null;
        String bitLYURL = BIT_LY_URL_PREFIX + longURL;
        try {

            if ((shortURL = getCacheURL(longURL)) == null) {
                String result = restUtil.executeGet(bitLYURL, null);
                JSONObject jobj = new JSONObject(result);
                int statusCode = jobj.getInt("status_code");
                if (statusCode != 200) {
                    throw new ShortURLException("ShortURL Service is temporary not available.");
                } else {
                    JSONObject dataObj = jobj.getJSONObject("data");
                    shortURL = dataObj.getString("url");
                    putShortURLinCache(longURL, shortURL);
                }
            }


        } catch (RestExecException ex) {
            throw new ShortURLException(ex.getMessage(), ex);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }

        return shortURL;
    }

    private String getCacheURL(String cacheKey) {
        String shortURL = null;
        try {
            myLog.debug("Try to get Cache Short URL for the key:" + cacheKey);
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null && cache.isElementInMemory(cacheKey) && cache.get(cacheKey) != null) {
                shortURL = (String) cache.get(cacheKey).getObjectValue();
            }
            myLog.debug("Cache Object, key:" + cacheKey + ", url->" + shortURL);
        } catch (CacheException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        return shortURL;

    }

    private void putShortURLinCache(String cacheKey, String shortURL) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.put(new Element(cacheKey, shortURL));
            }

        } catch (CacheException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }
}

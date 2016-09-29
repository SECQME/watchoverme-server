/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author coolboykl
 */
public class GoogleUrlShortenerService implements UrlShortenerService {

    public static final String LONG_URL_KEY = "longUrl";
    public static final String SHORT_URL_KEY = "id";
    private static final Logger myLog = Logger.getLogger(GoogleUrlShortenerService.class);
    private final static String CACHE_NAME = "googleShortURLCache";
    private static CacheManager cacheManager = null;
    private static int MAX_CACHE_OBJECT = 500;
    private static int CACHE_OBJ_EXPIRE_IN_SECOND = 86400;
    private RestUtil restUtil = null;
    private String googleShortURL = null;

    public GoogleUrlShortenerService(RestUtil restUtil, String googleShortURL) {
        this.restUtil = restUtil;
        this.googleShortURL = googleShortURL;
        cacheManager = CacheManager.create();
        if (cacheManager.getCache(CACHE_NAME) == null) {
            Cache newCache = new Cache(CACHE_NAME,
                    MAX_CACHE_OBJECT,
                    false,
                    false,
                    CACHE_OBJ_EXPIRE_IN_SECOND,
                    CACHE_OBJ_EXPIRE_IN_SECOND);
            cacheManager.addCache(newCache);
        }
    }

    @Override
    public String getShortURL(String longURL, String feature) throws ShortURLException {
        String shortURL = null;
        try {

            if ((shortURL = getCacheURL(longURL)) == null) {
                JSONObject reqObj = new JSONObject();
                reqObj.put(LONG_URL_KEY, longURL);
                myLog.debug("Calling to " + googleShortURL + " req:" + reqObj);
                String result = restUtil.executePost(googleShortURL, reqObj.toString(), null);
                myLog.debug("Short URL result->" + result);
                JSONObject resultObj = new JSONObject(result);
                shortURL = resultObj.getString(SHORT_URL_KEY);
                putShortURLinCache(longURL, shortURL);
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
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null && cache.isElementInMemory(cacheKey) && cache.get(cacheKey) != null) {
                shortURL = (String) cache.get(cacheKey).getObjectValue();
            }
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

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

import java.util.Properties;

/**
 *
 * @author jameskhoo
 */
public class BranchUrlShortenerService implements UrlShortenerService {

    private static final String ENDPOINT = "https://api.branch.io/v1/url";

    private RestUtil restUtil = null;
    private String apiKey = null;
    private static CacheManager cacheManager = null;
    private static final Logger myLog = Logger.getLogger(BranchUrlShortenerService.class);
    private final static String CACHE_NAME = "branchioCache";
    private static int MAX_CACHE_OBJECT = 500;
    private static int CACHE_OBJ_EXPIRE_IN_SECOND = 86400;

    public BranchUrlShortenerService(String apiKey, RestUtil restUtil) {
        this.restUtil = restUtil;
        this.apiKey = apiKey;
        cacheManager = CacheManager.create();
        Cache shortURLCache = new Cache(CACHE_NAME,
                MAX_CACHE_OBJECT,
                false,
                false,
                CACHE_OBJ_EXPIRE_IN_SECOND,
                CACHE_OBJ_EXPIRE_IN_SECOND);
        cacheManager.addCache(shortURLCache);
    }

    public String getShortURL(String longURL, String feature) throws ShortURLException {
        String shortURL = null;
        try {
            if ((shortURL = getCacheURL(longURL)) == null) {
                Properties header = new Properties();
                header.setProperty("Content-Type", "application/json");

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("sdk", "api");
                jsonRequest.put("branch_key", apiKey);
                jsonRequest.put("feature", feature);

                JSONObject data = new JSONObject();
                data.put("$fallback_url", longURL);
                jsonRequest.put("data", data.toString());

                String requestBody = jsonRequest.toString();
                myLog.debug("Branch.io request: " + requestBody);
                String result = restUtil.executePost(ENDPOINT, requestBody, header);

                myLog.debug("Branch.io result: " + result);
                JSONObject jsonResponse = new JSONObject(result);
                shortURL = jsonResponse.getString("url");
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

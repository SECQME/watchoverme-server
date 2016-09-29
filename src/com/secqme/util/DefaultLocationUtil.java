package com.secqme.util;

import com.secqme.CoreException;
import com.secqme.domain.dao.GoogleGeoCodeCacheDAO;
import com.secqme.domain.dao.GooglePlaceCacheDAO;
import com.secqme.domain.dao.TimeZoneDAO;
import com.secqme.domain.model.TimeZoneVO;
import com.secqme.domain.model.location.GoogleGeoCodeCacheVO;
import com.secqme.domain.model.location.GooglePlaceCacheVO;
import com.secqme.util.rest.RestExecException;
import com.secqme.util.rest.RestUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author coolboykl
 */
public class DefaultLocationUtil implements LocationUtil {

    private final static Logger myLog = Logger.getLogger(DefaultLocationUtil.class);
    private static final String GEO_PLUGIN_COUNTRY_CODE_KEY = "geoplugin_countryCode";
    private static final String CACHE_NAME = "gmapAddr";
    private static CacheManager cacheManager;
    private RestUtil restUtil = null;
    private String timezoneWebSrvURL = null;
    private String countryIPWebSrvURL = null;
    private String googleGeoCodeWebSrvURL = null;
    private TimeZoneDAO timeZoneDAO = null;
    //    private CountryLocationDAO countryLocationDAO = null;
    private DecimalFormat locationDecimalFormat = null;
    private DecimalFormat timeZoneDBKeyDecimalFormat = null;
    private String googlePlaceWebSrvURL = null;
    private GooglePlaceCacheDAO googlePlaceCacheDAO = null;
    private GoogleGeoCodeCacheDAO googleGeoCodeDAO = null;
    private Cache gmapAddrCache = null;


    public DefaultLocationUtil(RestUtil restUtil, String googleGeoCodeURL, String countryIPURL,
                               String timezoneWebSrvURL, String googlePlaceWebSrvURL,
                               TimeZoneDAO timeZoneDAO,
                               GooglePlaceCacheDAO googlePlaceCacheDAO,
                               GoogleGeoCodeCacheDAO googleGeoCodeDAO,
                               Integer maxCacheObj, Long expireInSeconds) {
        this.restUtil = restUtil;
        this.countryIPWebSrvURL = countryIPURL;
        this.timezoneWebSrvURL = timezoneWebSrvURL;
        this.googleGeoCodeWebSrvURL = googleGeoCodeURL;
        this.timeZoneDAO = timeZoneDAO;
//        this.countryLocationDAO = countryLocationDAO;
        locationDecimalFormat = new DecimalFormat("#.000");
        timeZoneDBKeyDecimalFormat = new DecimalFormat("#.00");
        this.googlePlaceWebSrvURL = googlePlaceWebSrvURL;
        this.googlePlaceCacheDAO = googlePlaceCacheDAO;
        this.googleGeoCodeDAO = googleGeoCodeDAO;

        cacheManager = CacheManager.create();
        gmapAddrCache = new Cache(CACHE_NAME,
                maxCacheObj,
                false,
                false,
                expireInSeconds,
                expireInSeconds);
        cacheManager.addCache(gmapAddrCache);
    }

//    public String getCountryCodeByRequestIP(String ipAddress) throws CoreException {
//        String countryCode = null;
//        String finalCountryIPCheckURL = countryIPWebSrvURL + ipAddress;
//        try {
//            String httpResult = restUtil.executeGet(finalCountryIPCheckURL, null);
//            // Example of the result->
//            // {
//            //  "geoplugin_region":"Kuala Lumpur",
//            //  "geoplugin_countryCode":"MY",
//            // "geoplugin_countryName":"Malaysia",
//
//            JSONObject resultObject = new JSONObject(httpResult);
//            countryCode = resultObject.getString(GEO_PLUGIN_COUNTRY_CODE_KEY);
//            if (StringUtils.isEmpty(countryCode)) {
//                countryCode = null;
//            }
//        } catch (RestExecException re) {
//            myLog.error("Error on getting Country Code for ip->" + ipAddress, re);
//        } catch (JSONException je) {
//            je.printStackTrace();
//        }
//        return countryCode;
//    }

    public String getCountryCodeByLocation(Double latitude, Double longitude) throws CoreException {
        String countryCode = null;
        String locationDBKey = timeZoneDBKeyDecimalFormat
                .format(latitude) + "," + timeZoneDBKeyDecimalFormat.format(longitude);
        if (locationDBKey.indexOf(".000,.000") > 0) {
            return null;
        }

        myLog.debug("Finding Country for this location:" + latitude + "," + longitude);
        GoogleGeoCodeCacheVO googleGeoCodeCacheVO = findGoogleGeoCodeResult(latitude, longitude);
        if (googleGeoCodeCacheVO != null) {
            countryCode = googleGeoCodeCacheVO.getCountryCode();
        }
        return countryCode;
    }

    @Override
    public String getTimeZone(Double latitude, Double longitude) throws CoreException {


        // https://maps.googleapis.com/maps/api/timezone/json?location=39.6034810,-119.6822510&timestamp=1331161200&sensor=true_or_false
        // Return
        // {
        //
        //   "dstOffset" : 0.0,
        //   "rawOffset" : -28800.0,
        //   "status" : "OK",
        //   "timeZoneId" : "America/Los_Angeles",
        //   "timeZoneName" : "Pacific Standard Time"
        // }
        String timeZoneID = null;
        String jsonResult = null;


        try {

            if (latitude == 0.00 & longitude == 0.00) {
                return null;
            }

            String locationDBKey = timeZoneDBKeyDecimalFormat.format(latitude) + "," + timeZoneDBKeyDecimalFormat.format(longitude);
            if (locationDBKey.indexOf(".000,.000") > 0) {
                return null;
            }

            TimeZoneVO timeZoneVO = timeZoneDAO.read(locationDBKey);
            if (timeZoneVO != null) {
                timeZoneID = timeZoneVO.getTimezoneid();
                myLog.debug("Found timeZone str from this location->" + locationDBKey + "->" + timeZoneID);
            } else {
                myLog.debug("Attempt to get TimeZone for this location:" + latitude + "," + longitude);
                String locationStr = latitude.toString() + "," + longitude.toString();
                long timestamp = (new Date().getTime() / 1000);
                String timestampStr = new Long(timestamp).toString();
                String url = timezoneWebSrvURL.replace("LOCATION", locationStr).replace("TIME_STAMP", timestampStr);
                jsonResult = restUtil.executeGet(url, null);
                JSONObject jobj = new JSONObject(jsonResult);
                timeZoneID = jobj.getString("timeZoneId");

                timeZoneVO = new TimeZoneVO();
                timeZoneVO.setLatlng(locationDBKey);
                timeZoneVO.setTimezoneid(timeZoneID);
                timeZoneDAO.create(timeZoneVO);
            }

        } catch (JSONException ex) {
            myLog.error("Error parsing json result" + jsonResult, ex);
        } catch (RestExecException ex) {
            myLog.error("Error on getting timeZone->", ex);
        }
        return timeZoneID;

    }

    public GooglePlaceCacheVO getGooglePlaceResult(String placeName) throws CoreException {
        GooglePlaceCacheVO googlePlaceVO = googlePlaceCacheDAO.findGooglePlaceByName(placeName);
        try {
            if (googlePlaceVO == null) {
                String encodedPlaceParameter = URLEncoder.encode(placeName, "UTF-8");
                String placeResultStr = restUtil.executeGet(googlePlaceWebSrvURL + encodedPlaceParameter, null);
                JSONObject placeResultObj = new JSONObject(placeResultStr);
                if (placeResultObj.has("results")) {
                    JSONArray placeArray = placeResultObj.getJSONArray("results");
                    JSONArray finalPlaceArray = new JSONArray();
                    for (int i = 0; i < placeArray.length(); i++) {
                        JSONObject placeObject = new JSONObject();
                        JSONObject googlePlaceObject = placeArray.getJSONObject(i);
                        placeObject.put("formatted_address", googlePlaceObject.getString("formatted_address"));
                        placeObject.put("name", googlePlaceObject.getString("name"));
                        placeObject.put("location", googlePlaceObject.getJSONObject("geometry").getJSONObject("location"));
                        finalPlaceArray.put(placeObject);
                    }
                    googlePlaceVO = new GooglePlaceCacheVO(placeName, finalPlaceArray.toString());
                    googlePlaceCacheDAO.create(googlePlaceVO);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            myLog.error("Error parsing place name: " + placeName, ex);
        } catch (RestExecException ex) {
            myLog.error("Error getting Google Place result: " + placeName, ex);
        } catch (JSONException ex) {
            myLog.error("Error parsing JSON from Google Place result for: " + placeName, ex);
        }
        return googlePlaceVO;
    }

    public double distanceBetweenTwoLocationInKM(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    public String getStreetAddr(Double latitude, Double longitude) {
        // TODO, needs a way to cache the return StreetAddr
        //
        //  {"status": "OK",
        //   "results": [ {
        // "types": [ "street_address" ],
        //            "formatted_address": "275-291 Bedford Ave, Brooklyn, NY 11211, USA",

        String streetAddr = null;
        String cacheKey = latitude + "," + longitude;
        String locationDBKey = parseLocationDBKey(latitude, longitude);
        if (locationDBKey.indexOf(".000,.000") > 0) {
            return null;
        }
        if ((streetAddr = (String) getCacheAddress(locationDBKey)) == null) {
            myLog.debug("Getting address from Google geocoding service, address.." + locationDBKey);

            String geoCodeURL = this.googleGeoCodeWebSrvURL +
                    +latitude
                    + ","
                    + longitude;
            try {
                String jsonResult = restUtil.executeGet(geoCodeURL, null);
                JSONObject jobj = new JSONObject(jsonResult);
                if (jobj.getString("status").equals("OK")) {
                    JSONArray resultArray = jobj.getJSONArray("results");
                    if (resultArray.length() > 0) {
                        JSONObject firstResult = resultArray.getJSONObject(0);

                        streetAddr = firstResult.getString("formatted_address");
                        putAddressInCache(cacheKey, streetAddr);

                        // Check if Existing GoogleGeoCode result exits, if not, parse the result
                        GoogleGeoCodeCacheVO googleGeoCodeVO = googleGeoCodeDAO.read(locationDBKey);
                        if (googleGeoCodeVO == null) {
                            JSONArray addressArray = firstResult.getJSONArray("address_components");
                            googleGeoCodeVO = parseGoogleGeoCodeResult(locationDBKey, latitude, longitude, addressArray);
                            googleGeoCodeDAO.create(googleGeoCodeVO);
                        }
                    }
                }
            } catch (RestExecException re) {
                myLog.error("Error on GeoCode for this location " + latitude + "," + longitude, re);
            } catch (JSONException je) {
                myLog.error("Error parsing GeoCode for this location " + latitude + "," + longitude, je);
            }
        } else {
            myLog.debug("Obtained address from Cache...");
        }
        return streetAddr;
    }

    private Object getCacheAddress(String cacheKey) {
        Object cacheObject = null;
        try {
            myLog.debug("Try to get Cache Object for the key:" + cacheKey);
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null && cache.isElementInMemory(cacheKey) && cache.get(cacheKey) != null) {
                cacheObject = cache.get(cacheKey).getObjectValue();
            } else {
                myLog.debug("Cache Object, key:" + cacheKey + " not found");
            }
        } catch (CacheException ex) {
            myLog.error("Failed to get cached address: " + cacheKey, ex);
        }
        return cacheObject;

    }

    private void putAddressInCache(String cacheKey, Object obj) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.put(new Element(cacheKey, obj));
            }
        } catch (CacheException ex) {
            myLog.error("Failed to get cached address: " + cacheKey, ex);
        }
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private String parseLocationDBKey(Double latitude, Double longitude) {
        return locationDecimalFormat.format(latitude) + "," + locationDecimalFormat.format(longitude);
    }

    private GoogleGeoCodeCacheVO findGoogleGeoCodeResult(Double latitude, Double longitude) {
        String locationDBKey = parseLocationDBKey(latitude, longitude);
        if (locationDBKey.indexOf(".000,.000") > 0) {
            return null;
        }
        GoogleGeoCodeCacheVO googleGeoCodeVO = googleGeoCodeDAO.read(locationDBKey);
        if (googleGeoCodeVO != null && googleGeoCodeVO.getCountryCode() != null) {
            myLog.debug("Found googleGeoCodeCache from our DB for this key:" + locationDBKey);
        } else {
            String finalURL = this.googleGeoCodeWebSrvURL + locationDBKey;
            try {
                String httpResult = restUtil.executeGet(finalURL, null);
                JSONObject resultObject = new JSONObject(httpResult);
                JSONArray resultArray = resultObject.getJSONArray("results");
                if (resultArray.length() > 0) {
                    JSONObject firstResult = resultArray.getJSONObject(0);
                    if (firstResult.has("address_components")) {
                        JSONArray addressArray = firstResult.getJSONArray("address_components");
                        googleGeoCodeVO = parseGoogleGeoCodeResult(locationDBKey, latitude, longitude, addressArray);
                        googleGeoCodeDAO.create(googleGeoCodeVO);
                    }
                }
            } catch (RestExecException re) {
                myLog.error("Error on GeoCode for this location " + latitude + "," + longitude, re);
            } catch (JSONException je) {
                myLog.error("Error parsing GeoCode for this location " + latitude + "," + longitude, je);
            }
        }
        return googleGeoCodeVO;
    }

    private GoogleGeoCodeCacheVO parseGoogleGeoCodeResult(String locationKey,
                                                          Double latitude,
                                                          Double longitude,
                                                          JSONArray addrArray) throws JSONException {
        GoogleGeoCodeCacheVO googleGeoCode = new GoogleGeoCodeCacheVO();
        googleGeoCode.setLatlng(locationKey);
        googleGeoCode.setLatitude(new Double(locationDecimalFormat.format(latitude)));
        googleGeoCode.setLongitude(new Double(locationDecimalFormat.format(longitude)));

        for (int i = 0; i < addrArray.length(); i++) {
            JSONObject addrObj = addrArray.getJSONObject(i);
            String addrShortName = addrObj.getString("short_name");
            String addrLongName = addrObj.getString("long_name");
            String addrType = addrObj.getJSONArray("types").toString();
            if (addrType.contains("neighborhood") || addrType.contains("sublocality")) {
                googleGeoCode.setNeighbourhood(addrLongName);
            }

            if (addrType.contains("locality")) {
                googleGeoCode.setCity(addrLongName);
            }

            if (addrType.contains("administrative_area_level_1")) {
                googleGeoCode.setState(addrLongName);
            }

            if (addrType.contains("country")) {
                googleGeoCode.setCountryCode(addrShortName);
                googleGeoCode.setCountry(addrLongName);
            }
        }

        return googleGeoCode;
    }
}

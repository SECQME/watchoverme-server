package com.secqme.rs.v2;

import java.util.ArrayList;
import java.util.List;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.safezone.SafeZoneVO;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * User: James Khoo
 * Date: 2/10/14
 * Time: 11:43 AM
 */
@Path("/v2/safezone")
public class SafeZoneResource extends BaseResource {
    private final static Logger myLog = Logger.getLogger(SafeZoneResource.class);

    public SafeZoneResource() {
        // Empty Constructor
    }



    @DELETE
    @Path("/{userAuthToken}/{zondId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteSafeZone(@PathParam("userAuthToken") String authToken, @PathParam("zondId") Long zoneId ) {
        String resultStr = null;
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Attempt to delete SafeZone:" + zoneId + " for user:" + userVO.getUserid());
            userVO = safeZoneManager.deleteSafeZone(userVO, zoneId);
            resultObject.put(CommonJSONKey.SAFE_ZONE_LIST_KEY, parseUserSafeZoneListAsJSONArray(userVO));
            populateUserSafetyLevelList(userVO, resultObject);
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;

    }

    /*
   {
     "zoneName":"My Home",
     "latitude":3.012345,
     "longitude":102.3413,
     "accuracy":50,
     "zoneAddress":"2 Jln 2/109"   //optional
   }
 */
    @PUT
    @Path("/{userAuthToken}/{zondId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateSafeZone(@PathParam("userAuthToken") String authToken, @PathParam("zondId") Long zoneId, String reqBody ) {
        String resultStr = null;
        try {
            JSONObject resultObject = new JSONObject();
            JSONObject reqObj = new JSONObject(reqBody);
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Attempt to update SafeZone:" + zoneId + " for user:" + userVO.getUserid());
            String zoneName = reqObj.has(CommonJSONKey.ZONE_NAME_KEY)? reqObj.getString(CommonJSONKey.ZONE_NAME_KEY) : null;
            String zoneAddress = reqObj.has(CommonJSONKey.ZONE_ADDRESS_KEY)? reqObj.getString(CommonJSONKey.ZONE_ADDRESS_KEY) : null;
            Double newLatitiude = reqObj.has(CommonJSONKey.LATITUDE_KEY)?  reqObj.getDouble(CommonJSONKey.LATITUDE_KEY) : null;
            Double newLongitude = reqObj.has(CommonJSONKey.LONGITUDE_KEY)? reqObj.getDouble(CommonJSONKey.LONGITUDE_KEY) : null;
            Double newAccuracy  = reqObj.has(CommonJSONKey.LOCATION_ACCURACY_KEY)? reqObj.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY) : null;
            Boolean autoConfirmSafety = reqObj.has(CommonJSONKey.AUTO_CONFIRM_SAFETY_KEY)? reqObj.getBoolean(CommonJSONKey.AUTO_CONFIRM_SAFETY_KEY) : null;
            userVO = safeZoneManager.updateSafeZone(userVO, zoneId, zoneName, zoneAddress, newLatitiude, newLongitude, newAccuracy, autoConfirmSafety);
            resultObject.put(CommonJSONKey.SAFE_ZONE_LIST_KEY, parseUserSafeZoneListAsJSONArray(userVO));
            populateUserSafetyLevelList(userVO, resultObject);
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;

    }


    @GET
    @Path("/{userAuthToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserSafeZoneList(@PathParam("userAuthToken") String authToken) {
        String resultStr = null;

        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Attempt to retrieves safeZoneList for user:" + userVO.getUserid());
            resultObject.put(CommonJSONKey.SAFE_ZONE_LIST_KEY, parseUserSafeZoneListAsJSONArray(userVO));
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;

    }

    /*
       {
         "zoneName":"My Home",
         "latitude":3.012345,
         "longitude":102.3413,
         "accuracy":50,
         "zoneAddress":"2 Jln 2/109"   //optional
         "autoConfirmSafety":true
       }
     */
    @POST
    @Path("/{userAuthToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewSafeZone(@PathParam("userAuthToken") String authToken, String reqBody) {
        String resultStr = null;

        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Attempt to add new SafeZone for user:" + userVO.getUserid() + " reqBody->" + reqBody);
            JSONObject reqObj = new JSONObject(reqBody);
            SafeZoneVO safeZoneVO = new SafeZoneVO(userVO, reqObj.getString(CommonJSONKey.ZONE_NAME_KEY),
                    reqObj.getDouble(CommonJSONKey.LATITUDE_KEY),
                    reqObj.getDouble(CommonJSONKey.LONGITUDE_KEY),
                    reqObj.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY),
                    reqObj.getBoolean(CommonJSONKey.AUTO_CONFIRM_SAFETY_KEY));
            if (reqObj.has(CommonJSONKey.ZONE_ADDRESS_KEY)) {
                safeZoneVO.setAddress(reqObj.getString(CommonJSONKey.ZONE_ADDRESS_KEY));
            }
            safeZoneManager.addNewSafeZone(userVO, safeZoneVO);
            userVO = userManager.getUserByActivationCode(authToken);
            resultObject.put(CommonJSONKey.SAFE_ZONE_LIST_KEY, parseUserSafeZoneListAsJSONArray(userVO));
            populateUserSafetyLevelList(userVO, resultObject);
            resultStr = resultObject.toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;

    }

    @POST
    @Path("/delete/{userToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteMultipleSafeZone(@PathParam("userToken") String userToken,
    		String reqBody) {
    	String resultStr = null;
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            myLog.debug("Attempt to delete multiple SafeZone for user:" + userToken + " body:" + reqBody);
            JSONObject reqObject = new JSONObject(reqBody);
            JSONArray safeZoneIdArray = reqObject.getJSONArray(CommonJSONKey.SAFE_ZONE_ID_LIST_KEY);
            
            if(safeZoneIdArray.length() > 0) {
            	List<Long> safeZoneIdList = new ArrayList<Long>();
            	for(int i=0;i<safeZoneIdArray.length();i++) {
            		safeZoneIdList.add(safeZoneIdArray.getLong(i));
            	}
            	userVO = safeZoneManager.deleteMultipleSafeZone(userVO, safeZoneIdList);
            }
            
            resultObject.put(CommonJSONKey.SAFE_ZONE_LIST_KEY, parseUserSafeZoneListAsJSONArray(userVO));
            populateUserSafetyLevelList(userVO, resultObject);
            resultStr = resultObject.toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }



}

package com.secqme.rs.v2;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.location.GooglePlaceCacheVO;
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
@Path("/v2/util")
public class UtilResource extends BaseResource {
    private final static Logger myLog = Logger.getLogger(UtilResource.class);

    public UtilResource() {
        // Empty Constructor
    }

    @POST
    @Path("/placesearch/{userAuthToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLocationPlace(@PathParam("userAuthToken") String authToken, String reqBody) {
        String resultStr = null;
        try {
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            JSONObject reqObj = new JSONObject(reqBody);
            String placeName = reqObj.getString(CommonJSONKey.PLACE_NAME_KEY);
            GooglePlaceCacheVO placeCacheVO = locationUtil.getGooglePlaceResult(placeName);
            JSONArray placeArray = new JSONArray(placeCacheVO.getResult());
            resultStr = new JSONObject().put(CommonJSONKey.GOOGLE_PLACE_ARRAY_KEY, placeArray).toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;
    }

    /*
       {
        "mobileCountry":"MY",
        "mobileNumber":"162389788"
        }
     */

    @POST
    @Path("/mobilepin/{appid}/{appsecret}")
    @Produces(MediaType.APPLICATION_JSON)
    public String generateMobilePin(@PathParam("appid") String applicationId,
                                    @PathParam("appsecret") String applicationSecret,
                                    String reqBody) {
        String result = "";
        String userMobileNumber;
        String userMobileCountry;
        try {
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            JSONObject reqObj = new JSONObject(reqBody);
            userMobileCountry = reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
            userMobileNumber = reqObj.getString(CommonJSONKey.MOBILE_NO_KEY);
            String mobilePin = userManager.generateMobileVerificationPin(userMobileCountry, userMobileNumber, "en_US");
            JSONObject resultObj = new JSONObject().put(CommonJSONKey.MOBILE_VERIFY_PIN_KEY, mobilePin);
            result = resultObj.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return result;
    }


    /*
       {
        "mobileCountry":"MY",
        "mobileNumber":"162389788",      // OR
        "emailAddr":"james.khoo@secq.me"
        }
     */

    @POST
    @Path("/passwordresetpin/{appid}/{appsecret}")
    @Produces(MediaType.APPLICATION_JSON)
    public String generatePasswordResetPin(@PathParam("appid") String applicationId,
                                           @PathParam("appsecret") String applicationSecret,
                                           String reqBody) {
        String result = "";
        String userMobileNumber = null;
        String userMobileCountry = null;
        String userEmailAddr = null;
        try {
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            JSONObject reqObj = new JSONObject(reqBody);
            if (reqObj.has(CommonJSONKey.MOBILE_COUNTRY_KEY)) {
                userMobileCountry = reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
                userMobileNumber = reqObj.getString(CommonJSONKey.MOBILE_NO_KEY);
            } else {
                userEmailAddr = reqObj.getString(CommonJSONKey.EMAIL_ADDR_KEY);
            }
            String passwordResetPin =
                    userManager.forgotPasswordUsingMobilePin(userMobileCountry, userMobileNumber, userEmailAddr);
            JSONObject resultObj = new JSONObject().put(CommonJSONKey.PASSWORD_RESET_PIN_KEY, passwordResetPin);
            result = resultObj.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return result;
    }
    
    /*
    { // test push to parse
     }
  */

	@POST
	@Path("/parse")
	@Produces(MediaType.APPLICATION_JSON)
	public String pushToParse(String reqBody) {
		String result = "";
		try {
			myLog.debug("pushToParse ");
			JSONObject reqObj = new JSONObject(reqBody);
			String authToken = reqObj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
			String message = reqObj.getString("message");
			String data = reqObj.getString("data");
			UserVO userVO = userManager.getUserByActivationCode(authToken);
			userManager.pushToParse(userVO, message, data);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
		}
		return result;
	}
}

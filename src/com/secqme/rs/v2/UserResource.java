package com.secqme.rs.v2;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.ContactInvitationStatus;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.event.FullEventInfoVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.promotion.PromotionVO;
import com.secqme.manager.UserManager;
import com.secqme.rs.BaseResource;
import com.secqme.util.marketing.MarketingCampaign;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

@Path("/v2/user")
public class UserResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(UserResource.class);

    public UserResource() {
    }

    @POST
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String verifyUser(String reqBody) {
        String userPassword = null;
        JSONObject verifyResultObj = new JSONObject();
        try {
            verifyResultObj.put(CommonJSONKey.VALID_ACCT_KEY, false);
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(CommonJSONKey.EMAIL_ADDR_KEY)) {
                String emailAddr = jobj.getString(CommonJSONKey.EMAIL_ADDR_KEY);
                userVO = userManager.getUserInfoByUserId(emailAddr);
            }
            String deviceName = null;
            if (jobj.has(CommonJSONKey.DEVICE_KEY)) {
                deviceName = jobj.getString(CommonJSONKey.DEVICE_KEY);
                myLog.debug("Device Name : " + deviceName);
            }
            userPassword = jobj.getString(CommonJSONKey.USER_PASSWORD_KEY);
            UserVO newUser = userManager.authenticateUserByUserId(userVO.getUserid(), userPassword);
            if (newUser != null && newUser.getUserid().equalsIgnoreCase(userVO.getUserid())) {
                userVO.setPackageVO(newUser.getPackageVO());
                verifyResultObj.put(CommonJSONKey.VALID_ACCT_KEY, true);
                verifyResultObj.put(CommonJSONKey.AUTH_TOKEN_KEY, userVO.getActivationCode());
                if (deviceName != null) {
                    userVO.setDevice(deviceName);
                    userManager.trimNameAndMobileNo(userVO);
                    myLog.debug("Updating user's device:" + deviceName);
                }
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return verifyResultObj.toString();

    }

    @POST
    @Path("/verifySNSLogin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String verifySnsUser(String reqBody, @Context HttpServletRequest httpReq) {

        /* Example of parameters pass in
           {
            "snsName":"facebook",
            "snsId":"100000003453455", 
            "device":"android"

            }
        */

        myLog.debug("Verifying user request->" + reqBody);
        JSONObject verifyResultObj = new JSONObject();

        try {
            verifyResultObj.put(CommonJSONKey.VALID_ACCT_KEY, false);
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;

            String snsId = jobj.getString(CommonJSONKey.SNS_ID);
            String snsName = jobj.getString(CommonJSONKey.SNS_NAME);

            // take the SNS accessToken validate against SNS userId
            UserSNSConfigVO userSNSConfigVO = userManager.getUserSnsConfigVOBySnsuidAndSnsName(snsId, snsName);

            if (userSNSConfigVO != null) {
                userVO = userManager.getUserInfoByUserId(userSNSConfigVO.getUserid());
                if (userVO != null) {
                    // response with activationCode so that user can directly login
                    verifyResultObj.put(CommonJSONKey.AUTH_TOKEN_KEY, userVO.getActivationCode());
                    verifyResultObj.put(CommonJSONKey.VALID_ACCT_KEY, true);
                    verifyResultObj.put(CommonJSONKey.USER_ID_KEY, userVO.getUserid());
                    if (userVO.getMobileNo() != null) {
                        verifyResultObj.put(CommonJSONKey.MOBILE_COUNTRY_KEY, userVO.getMobileCountry().getCallingCode());
                        verifyResultObj.put(CommonJSONKey.MOBILE_NO_KEY, userVO.getMobileNo());
                    }
                }
            } else {
                // response back so that user key in password for our server account
                verifyResultObj.put(CommonJSONKey.STATUS, "NEW_USER");
            }
//            else {
//                throw new ConflictException("Social Network Login Failed: invalid access token");
//            }

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return verifyResultObj.toString();
    }

    /*
      {
        "name":"James Khoo",
        "device":"android",
        "langCode":"en",
        "mobileCountry":"MY",
        "mobileNumber":"162389788",
        "location":{"lat":3.06766,"lng":101.62},
        "networkContact":true
      }
     */
    @POST
    @Path("/temporary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createTemporaryUser(String reqBody) {
        String userMobileNumber;
        String userMobileCountry;
        JSONObject resultObj = new JSONObject();
        Double latitude = null;
        Double longitude = null;
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            String userName = reqObj.getString(CommonJSONKey.USER_NAME_KEY);
            String userDevice = reqObj.getString(CommonJSONKey.DEVICE_KEY);
            String userLanguage = reqObj.getString(CommonJSONKey.LANG_CODE_KEY);

            if (reqObj.has(CommonJSONKey.LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = reqObj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
            }

            userMobileCountry = reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
            userMobileNumber = reqObj.getString(CommonJSONKey.MOBILE_NO_KEY);
            UserVO userVO = userManager.registerTemporaryUser(userName,
                    userMobileCountry, userMobileNumber,
                    userLanguage, userDevice, latitude, longitude);
            resultObj.put(CommonJSONKey.AUTH_TOKEN_KEY, userVO.getActivationCode());
            resultObj.put(CommonJSONKey.USER_ID_KEY, userVO.getUserid());

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }

    /*
       {
         "authToken":"userToken",
          "name":"James Khoo",
          "mobileCountry":"MY",
          "mobileNumber":"162389788",
          "password":"secqme123",
          "emailAddr":"james.khoo@secq.me"  // optional
        }
     */

    @POST
    @Path("/temporary/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String activateTemporaryUser(String reqBody) {
        JSONObject resultObj = new JSONObject();
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            String authToken = reqObj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String userName = reqObj.getString(CommonJSONKey.USER_NAME_KEY);
            String userPassword = reqObj.getString(CommonJSONKey.USER_PASSWORD_KEY);
            String userEmailAddr = reqObj.has(CommonJSONKey.EMAIL_ADDR_KEY) ? reqObj.getString(CommonJSONKey.EMAIL_ADDR_KEY) : null;
            String userMobileCountry = reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
            String userMobileNumber = reqObj.getString(CommonJSONKey.MOBILE_NO_KEY);

            UserVO userVO = userManager.activateTemporaryUser(authToken, userMobileCountry, userMobileNumber, userName, userEmailAddr,
                    userPassword);

            // Check if the pass in request have
            prepareUserLoginInfo(resultObj, userVO);

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();

    }

    /*
    {
     "snsUID":"543445342453";
     "snsAdditionalConfig":{
                            "userProfileObj":
                                  { "timezone":2,
                                    "username":"myFBNAme",
                                    "email":"myFaceBookProfile",
                                    "name":"Blah Blah",
                                    "gender":"male"
                                    },
                            "access_token":"FBAccessToken"
                            }
     }


  // Sign up

     "userSNSConfig":{    // Optional, if user signup via Facebook
               "snsUID":"570008013",
               "snsName":"facebook",
               "notify":true,
               "snsAdditionalConfig":{
                    "userProfileObj":{
                        "timezone":8,
                        "username":"coolboykl",
                        "email":"khoo.james@gmail.com"
                     }
                     "locale":"en_US",
                     "link":"http://www.facebook.com/coolboykl",
                     "name":"Khoo Chen Shiang",
                     "gender":"male",
                     "access_token":"XDFFG342335Q060WGZD",
               }
     }
     */
    @POST
    @Path("/sns/facebook/{authToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String connectWithFaceBook(@PathParam("authToken") String authToken, String reqBody) {
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            UserSNSConfigVO configVO = new UserSNSConfigVO();
            configVO.setSocialNetworkVO(fbUtil.getFbSNSVO());
            configVO.setSnsName(fbUtil.getFbSNSVO().getSnsName());
            configVO.setUpdatedDate(new Date());
            configVO.setSnsuid(reqObj.getString(CommonJSONKey.SNS_UID_KEY));
            configVO.setAdditionalConfig(reqObj.getJSONObject(CommonJSONKey.SNS_ADDITONAL_CONFIG_KEY));
            configVO.setUserVO(userVO);
            configVO.setNotify(Boolean.TRUE);
            userManager.addReplaceSNSConfig(userVO, configVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return OK_STATUS;
    }

    /*
     For WOM version 2.5
     Use this API
     {
       "name":"James Khoo",
       "mobileCountry":"MY",
       "mobileNumber":"162389788",
       "password":"secqme123",
       "emailAddr":"james.khoo@secq.me",// optional field
       "device":"android",
       "langCode":"en",
       "pushMessageToken":"ESDFSDFSDFSD", // iOS, or Android push message Token
       "messageTokenType":"iOS", // either 'iOS' or 'Android'
       "location":{"lat":3.06766,"lng":101.62},
        "userSNSConfig":{    // Optional
             "snsUID":"570008013",
             "snsName":"facebook",
             "notify":true,
             "snsAdditionalConfig":{
                "userProfileObj":{
                    "timezone":8,
                    "username":"coolboykl",
                    "email":"khoo.james@gmail.com"
                  }
                  "access_token":"XDFFG342335Q060WGZD",
                "locale":"en_US",
                "link":"http://www.facebook.com/coolboykl",
                "name":"Khoo Chen Shiang",
                "gender":"male"
        }
     }
    */
    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String signUpUser(@Context HttpServletRequest httpServletRequest, String reqBody) {
        String userMobileNumber;
        String userMobileCountry;
        JSONObject resultObj = new JSONObject();
        Double latitude = null;
        Double longitude = null;
        JSONObject userSNSConfigObj = null;
        double clientVersion = 0;
        String pictureStream = null;
        try {
            JSONObject reqObj = new JSONObject(reqBody);

            String userName = reqObj.getString(CommonJSONKey.USER_NAME_KEY);
            String userDevice = getClientOS(httpServletRequest,reqObj);
            String userLanguage = reqObj.has(CommonJSONKey.LANG_CODE_KEY) ? reqObj.getString(CommonJSONKey.LANG_CODE_KEY) : null;
            String userPassword = reqObj.getString(CommonJSONKey.USER_PASSWORD_KEY);
            String userEmailAddr = reqObj.has(CommonJSONKey.EMAIL_ADDR_KEY) ? reqObj.getString(CommonJSONKey.EMAIL_ADDR_KEY) : null;

            if (reqObj.has(CommonJSONKey.LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = reqObj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
            }

            userMobileCountry = reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
            userMobileNumber = reqObj.getString(CommonJSONKey.MOBILE_NO_KEY);
            if (reqObj.has(CommonJSONKey.USER_SNS_CONFIG_KEY)) {
                userSNSConfigObj = reqObj.getJSONObject(CommonJSONKey.USER_SNS_CONFIG_KEY);
            }
            if (reqObj.has(CommonJSONKey.CLIENT_VERSION_KEY)) {
            	clientVersion = reqObj.getDouble(CommonJSONKey.CLIENT_VERSION_KEY);
            }
            if (reqObj.has(CommonJSONKey.PICTURE_STREAM_KEY)) {
            	pictureStream = reqObj.getString(CommonJSONKey.PICTURE_STREAM_KEY);
            }
            UserVO userVO = userManager.registerUser(userName, userPassword,
                    userEmailAddr, userMobileCountry, userMobileNumber, userLanguage, userDevice,
                    latitude, longitude, userSNSConfigObj, clientVersion, pictureStream);
            if (reqObj.has(CommonJSONKey.CLIENT_VERSION_KEY)) {
                userManager.checkContactAddedList(userVO);
            }
            if(userVO.getContactList() != null) {
            	myLog.debug("new user contactList size1 :" + userVO.getContactList().size());
            }
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            if (reqObj.has(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY) && reqObj.has(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY)) {
                userManager.updateUserPushMessageToken(userVO,
                        reqObj.getString(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY), reqObj.getString(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY));
            }
            if(userVO.getContactList() != null) {
            	myLog.debug("new user contactList size2 :" + userVO.getContactList().size());
            }
            //check if user already has been added by someone

            // Check if the pass in request have
            prepareUserLoginInfo(resultObj, userVO);

            // Remove from Emergency Contact funnel
            userManager.deleteMarketingEmailContact(userVO, MarketingCampaign.EMERGENCY_CONTACT_FUNNEL);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }


     /*
       {
          "mobileCountry":"MY",
          "mobileNumber":"162389788",
        }
     */

    @POST
    @Path("/mobilenumber/{authToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addMobileNumberForEmailUser(String reqBody, @PathParam("authToken") String authToken) {
        JSONObject resultObj = new JSONObject();
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            String userMobileCountry = reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
            String userMobileNumber = reqObj.getString(CommonJSONKey.MOBILE_NO_KEY);

            UserVO userVO = userManager.getUserByActivationCode(authToken);
            userManager.updateMobileNumber(userVO, userMobileCountry, userMobileNumber);
            prepareUserLoginInfo(resultObj, userVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();

    }

    /*
      For WOM version 2.0 earlier
      {
      "userid":"khoo.james@gmail.com",
      "password":"ftooil",
      "nickName":"Khoo Chen Shiang",
      "device":"android",
      "location":{"lat":3.06766,"lng":101.62},
      "langCode":"en_US",
        "userSNSConfig":{    // Optional
        "snsUID":"570008013",
        "access_token":"XDFFG342335Q060WGZD",
        "snsName":"facebook",
        "notify":true,
        "snsAdditionalConfig":{
            "userProfileObj":{
            "timezone":8,
            "username":"coolboykl",
            "email":"khoo.james@gmail.com",
            "location":{
            "id":"106031246101831",
            "name":"Kuala Lumpur, Malaysia"
         },
          "locale":"en_US",
          "link":"http://www.facebook.com/coolboykl",
          "name":"Khoo Chen Shiang",
          "gender":"male"
        }
       }
      }
      }
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUserWithEmailAddress(String reqBody, @Context HttpServletRequest httpReq) {
        myLog.debug("Registring new user, req->" + reqBody + httpReq);
        JSONObject resultObj = new JSONObject();
        try {
            resultObj.put("status", "ok");
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = new UserVO();
            userVO.setUserid(jobj.getString(CommonJSONKey.USER_ID_KEY));
            userVO.setNickName(jobj.getString(CommonJSONKey.USER_NICK_NAME_KEY));
            userVO.setPassword(jobj.getString(CommonJSONKey.USER_PASSWORD_KEY));
            String userCountryCode = null;

            if (jobj.has(CommonJSONKey.LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = jobj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                Double latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                Double longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
                userCountryCode = locationUtil.getCountryCodeByLocation(latitude, longitude);
                userVO.setTimeZone(locationUtil.getTimeZone(latitude, longitude));
            }

            if (userCountryCode != null) {
                userVO.setCountryVO(smsManager.getCountry(userCountryCode));
                myLog.debug("Getting Country VO, code->" + userCountryCode);
            }

            if (jobj.has(CommonJSONKey.MOBILE_NO_KEY)) {
                userVO.setMobileNo(jobj.getString(CommonJSONKey.MOBILE_NO_KEY));
            }

            if (jobj.has(CommonJSONKey.DEVICE_KEY)) {
                userVO.setDevice(jobj.getString(CommonJSONKey.DEVICE_KEY));
            }

            if (jobj.has(CommonJSONKey.LANG_CODE_KEY)) {
                userVO.setLangCode(jobj.getString(CommonJSONKey.LANG_CODE_KEY));
            }

            userManager.registerUser(userVO.getNickName(), userVO.getPassword(), userVO.getEmailAddress(),
                    userCountryCode, userVO.getMobileNo(), userVO.getLangCode(), userVO.getDevice(),
                    userVO.getLatitude(), userVO.getLongitude(), jobj.optJSONObject(CommonJSONKey.USER_SNS_CONFIG_KEY),
                    userVO.getClientVersion(), null);

            // Add default event list to user
            // Check if the pass in request have
            resultObj.put(CommonJSONKey.AUTH_TOKEN_KEY, userVO.getActivationCode());
            resultObj.put(CommonJSONKey.USER_ID_KEY, userVO.getUserid());
            if (userVO.getCountryVO() != null) {
                resultObj.put(CommonJSONKey.USER_COUNTRY_KEY, userVO.getCountryVO().getIso());
            }

            PromotionVO promotionVO = promotionManager.getUserCurrentPromotion(userVO);
            if (promotionVO != null) {
                resultObj.put("promotion", promotionVO.toJSONObject());
            }

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }

    @GET
    @Path("/available/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String checkUserNameAvailable(@PathParam("userid") String userid) {
        UserVO userVO = null;
        String message = null;
        try {
            userVO = userManager.getUserInfoByUserId(userid);
            message = "{\"available\":false}";
        } catch (CoreException e) {
            message = "{\"available\":true}";
        }
        return message;
    }

    /**
     * {
     * "userid":"james.khoo@secq.me"
     * "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084"
     * }
     *
     * @param reqBody
     * @return
     * @throws com.secqme.CoreException
     */
    @POST
    @Path("/activatelitepkg")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String activateLitePackage(String reqBody) throws CoreException {
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            String userToken = reqObj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String reqUserID = reqObj.getString(CommonJSONKey.USER_ID_KEY);
            UserVO userVO = userManager.authenticateUserByActivationCode(userToken);
            billingManager.activateFreeMiumPackage(userVO, true);

        } catch (JSONException ex) {
            myLog.error("Error on activating LITE PACKAGE for user", ex);
        }
        return OK_STATUS;
    }

    /**
     * {
     * "userid":"james.khoo@secq.me"
     * "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084"
     * }
     *
     * @param reqBody
     * @return
     * @throws com.secqme.CoreException
     */
    @POST
    @Path("/activatetrial")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String activateTrialPackage(String reqBody) throws CoreException {
        JSONObject resultObj = new JSONObject();
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            String userToken = reqObj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String reqUserID = reqObj.getString(CommonJSONKey.USER_ID_KEY);
            UserVO userVO = userManager.authenticateUserByActivationCode(userToken);
            billingManager.activateTrialPackageForUser(userVO);
            prepareUserLoginInfo(resultObj, userVO);

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }

    /*
     * pre with tokenKey, and userid, sample
      {
        "userid":"james.khoo@secq.me",
        "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084",
        "ipAddress":"49.181.63.173",
        "location":{"latitude":3.06766,"longitude":101.62},
        "pushMessageToken":"ESDFSDFSDFSD", // iOS, or Android push message Token
        "messageTokenType":"iOS", // either 'iOS' or 'Android'
      }
     * <p/>
     * } "
     *
     * @param reqBody
     * @return
     * @throws CoreException
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String loginUser(String reqBody) throws CoreException {
        myLog.debug("Login for new user, req->" + reqBody);
        JSONObject resultObj = new JSONObject();
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            String userToken = reqObj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String reqUserID = reqObj.getString(CommonJSONKey.USER_ID_KEY);
            double clientVersion = 1;

            UserVO userVO = userManager.authenticateUserByActivationCode(userToken);

            if (reqObj.has(CommonJSONKey.LOCATION_KEY)) {
                JSONObject locationObj = reqObj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                double latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                double longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);

                userManager.updateLocation(userVO, latitude, longitude, true);
            }

            if (reqObj.has(CommonJSONKey.CLIENT_VERSION_KEY)) {
                userManager.checkForClientVersionUpgrade(userVO, reqObj.getDouble(CommonJSONKey.CLIENT_VERSION_KEY));
            }
            
            if (reqObj.has(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY) && reqObj.has(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY)) {
                userManager.updateUserPushMessageToken(userVO,
                        reqObj.getString(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY), reqObj.getString(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY));
            }

            generateDeviceAnalyticsToken(userVO, reqObj);
            prepareUserLoginInfo(resultObj, userVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultObj.toString();
    }

    /*
       {
         "emailAddr":"james.khoo@secq.me",    // or
         "mobileCountry":"MY",
         "mobileNumber":"162389788",
         "password":"mypassword",
         "pushMessageToken":"ESDFSDFSDFSD", // iOS, or Android push message Token
         "messageTokenType":"iOS", // either 'iOS' or 'Android'
       }

     */
    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String authenticateUser(String reqBody) throws CoreException {
        JSONObject resultObj = new JSONObject();
        String userid;
        String userPassword;
        UserVO userVO;
        double clientVersion;
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            userPassword = reqObj.getString(CommonJSONKey.USER_PASSWORD_KEY);
            if (reqObj.has(CommonJSONKey.EMAIL_ADDR_KEY)) {
                userid = reqObj.getString(CommonJSONKey.EMAIL_ADDR_KEY);
                myLog.debug("authenticate user via email - " + userid);
                userVO = userManager.authenticateUserByUserId(userid, userPassword);
            } else {
            	 myLog.debug("authenticate user via mobile - " + reqObj.getString(UserVO.MOBILE_COUNTRY_KEY) 
            			 + "-" + reqObj.getString(UserVO.MOBILE_NO_KEY));
                userVO = userManager.authenticateUserByUserId(userManager.getUserIdFromMobileNumber(reqObj.getString(UserVO.MOBILE_COUNTRY_KEY), reqObj.getString(UserVO.MOBILE_NO_KEY)), userPassword);
            }

            if (reqObj.has(CommonJSONKey.CLIENT_VERSION_KEY)) {
                userManager.checkForClientVersionUpgrade(userVO, reqObj.getDouble(CommonJSONKey.CLIENT_VERSION_KEY));
            }

            if (reqObj.has(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY) && reqObj.has(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY)) {
                userManager.updateUserPushMessageToken(userVO,
                        reqObj.getString(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY), reqObj.getString(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY));
            }

            prepareUserLoginInfo(resultObj, userVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }

    private void prepareUserLoginInfo(JSONObject resultObj, UserVO userVO) throws JSONException, CoreException {
        SecqMeEventVO eventVO = eventManager.getUserLatestEvent(userVO.getUserid());
        if (eventVO != null && eventVO.isEventRunning()) {
            FullEventInfoVO fullEventInfoVO = eventManager.getFullEventInfo(userVO, eventVO.getId());
            resultObj.put(CommonJSONKey.USER_LATEST_EVENT_KEY, fullEventInfoVO.toJSON());
        }
        resultObj.put(CommonJSONKey.AUTH_TOKEN_KEY, userVO.getActivationCode());
        resultObj.put(CommonJSONKey.USER_ID_KEY, userVO.getUserid());
        resultObj.put(CommonJSONKey.EMAIL_ADDR_KEY, userVO.getEmailAddress());
        resultObj.put(UserVO.USER_ID_KEY, userVO.getUserid());
        resultObj.put(BillingPkgVO.PACKAGE_NAME_KEY, userVO.getPackageVO().getPkgName());
        resultObj.put(CommonJSONKey.BILL_PKG_TYPE_KEY, userVO.getPackageVO().getPkgType().toString());
        resultObj.put(BillingPkgVO.ACCESS_RIGHT, userVO.getPackageVO().getAccessRightJSON());
        resultObj.put(CommonJSONKey.MAX_CONTACT_ALLOWS, userVO.getPackageVO().getMaxSMSContactAllow());
        resultObj.put(CommonJSONKey.TOTAL_EVENT_REGISTERED_KEY, eventManager.getUserTotalRegisteredEvent(userVO.getUserid()));
        boolean facebookConnected = false;
        boolean facebookNotify = false;
        if (userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
            List<UserSNSConfigVO> userSNSConfigVOs = userVO.getSnsConfigList();
            JSONArray userSNSConfigs = new JSONArray();

            for (UserSNSConfigVO snsConfigVO : userVO.getSnsConfigList()) {
                if (snsConfigVO.getSnsName().equalsIgnoreCase("facebook")) {
                    facebookConnected = true;
                    facebookNotify = snsConfigVO.isNotify();
                }
                userSNSConfigs.put(snsConfigVO.toJSONObj());
            }

            resultObj.put(CommonJSONKey.USER_SNS_CONFIGS_KEY, userSNSConfigs);
        }

        resultObj.put(CommonJSONKey.FACEBOOK_CONNECTED_KEY, facebookConnected);
        if (facebookConnected) {
            resultObj.put(CommonJSONKey.FACEBOOK_NOTIFY_KEY, facebookNotify);
        }

        if (userVO.getTrialPackageActivatedAt() != null) {
            resultObj.put(CommonJSONKey.TRIAL_PACKAGE_ACTIVATED_KEY, Boolean.TRUE);
            resultObj.put(CommonJSONKey.TRIAL_PACKAGE_ACTIVATED_AT, userVO.getTrialPackageActivatedAt().getTime());
        } else {
            if (!BillingPkgType.PREMIUM.equals(userVO.getPackageVO().getPkgType())) {
                resultObj.put(CommonJSONKey.TRIAL_PACKAGE_ACTIVATED_KEY, Boolean.FALSE);
                String trialPackageMarketingText = billingManager.getTrialPackageMarketingTextJSON(userVO);
                if (StringUtils.isNotEmpty(trialPackageMarketingText)) {
                    JSONObject trialPkgJSON = new JSONObject(trialPackageMarketingText);
                    resultObj.put(CommonJSONKey.TRIAL_PACKAGE_MARKETING_TEXT_KEY, trialPkgJSON);
                }
            }
        }

        JSONArray contactJSONArray = null;
        if (userVO.getContactList() != null && userVO.getContactList().size() > 0) {
        	contactJSONArray = parseUserContactListAsJSONArray(userVO);
            resultObj.put(CommonJSONKey.CONTACT_LIST_KEY, contactJSONArray);

            // network contact - return true if user has contact that still use old version app
            // Note: Network contact feature was removed due security/privacy issues.
            for (ContactVO contactVO : userVO.getContactList()) {
                UserVO contactUserVO = contactVO.getContactUserVO();
            	if (contactUserVO != null && contactUserVO.getClientVersion() < 6) {
            		resultObj.put(CommonJSONKey.NOTIFY_CONTACT_KEY, true);
            	}
            }
        }
        
        //contact user that has event running
        resultObj.put(CommonJSONKey.CONTACT_EVENT_LIST_KEY, parseRunningEventContactListAsJSONArray(userVO));
        
        if (userVO.getUserCreatedSafeZoneList() != null && userVO.getUserCreatedSafeZoneList().size() > 0) {
            resultObj.put(CommonJSONKey.SAFE_ZONE_LIST_KEY, parseUserSafeZoneListAsJSONArray(userVO));
        }


        if (userVO.getReferralURL() != null) {
            resultObj.put(CommonJSONKey.USER_REFERRAL_URL_KEY, userVO.getReferralShortURL());
        }

        BillingCycleVO billCycleVO = billingManager.getUserLatestBillCycleVO(userVO);
        if (billCycleVO != null) {
            if (billCycleVO.getEndDate() != null) {
                resultObj.put(BillingCycleVO.BILL_EXPIRED_AT_KEY, billCycleVO.getEndDate().getTime());
                Date toDay = new Date();
                Long diffTime = billCycleVO.getEndDate().getTime() - toDay.getTime();
                Long diffDays = (diffTime / (24 * 60 * 60 * 1000)) + 1;
                resultObj.put(CommonJSONKey.BILL_PKG_EFFECTIVE_DAYS_KEY, diffDays);
            }

            // REMARK Needs to put the object as a String,  as
            // Sometime user is assigned to billing package with unlimited credit
            //
            resultObj.put(BillingCycleVO.EVENT_CREDIT_BALANCE_KEY,
                    billCycleVO.getEventCreditBalanceText());
            resultObj.put(BillingCycleVO.SMS_CREDIT_BALANCE_KEY,
                    billCycleVO.getSMSCreditBalanceText());
            if (userVO.getUserCurrentSubscription() != null) {
                resultObj.put(CommonJSONKey.SUBSCRIPTION_PKG_KEY, userVO.getUserCurrentSubscription().toJSON());
            } else {
                PromotionVO promotionVO = promotionManager.getUserCurrentPromotion(userVO);
                if (promotionVO != null) {
                    resultObj.put("promotion", promotionVO.toJSONObject());
                } else {
                    populateUserBillingPackageList(userVO, resultObj);
                    String premiumMarketText = billingManager.getPremiumMarketTextConfigJSON(userVO);
                    if (StringUtils.isNotEmpty(premiumMarketText)) {
                        resultObj.put(CommonJSONKey.PREMIUM_PKG_CONFIG,
                                new JSONObject(premiumMarketText));
                    }
                }
            }


            if (userVO.getCountryVO() != null) {
                resultObj.put(CommonJSONKey.USER_COUNTRY_KEY, userVO.getCountryVO().getIso());
            }

            if (userVO.getMobileCountry() != null) {
                resultObj.put(CommonJSONKey.MOBILE_COUNTRY_KEY, userVO.getMobileCountry().getIso());
                resultObj.put(CommonJSONKey.MOBILE_NO_KEY, userVO.getMobileNo());
                resultObj.put(CommonJSONKey.I18N_MOBILE_NUMBER_KEY, "+" +
                        userVO.getMobileCountry().getCallingCode() + "-" + userVO.getMobileNo());
            }
            if (userVO.getNickName() != null) {
                resultObj.put(CommonJSONKey.USER_NAME_KEY, userVO.getNickName());
            }

        }
        resultObj.put(CommonJSONKey.SERVER_VERSION_KEY, userManager.getServerParam(CommonJSONKey.SERVER_VERSION_KEY));
        //safety level percentage
        populateUserSafetyLevelList(userVO, resultObj);
        //profile pic url
        resultObj.put(CommonJSONKey.PROFILE_PICTURE_URL_KEY, userVO.getProfilePictureURL());
        resultObj.put(CommonJSONKey.ANALYTICS_IDS_KEY, userVO.getAnalyticsIds());
    }

    private void populateUserBillingPackageList(UserVO userVO, JSONObject resultObj) throws JSONException {
        // User don't have any active subscription
        // retrive all available Billing Packages for user to consider;
        // paypal
        List<PricingPackageVO> autoRenewPricePkgList =
                paymentManager.getAutoRenewPricingPackage(userVO.getMarketVO().getName());

        if (autoRenewPricePkgList != null && autoRenewPricePkgList.size() > 0) {
            JSONArray pricePkgArray = new JSONArray();
            for (PricingPackageVO vo : autoRenewPricePkgList) {
                pricePkgArray.put(vo.toJSONObject());

            }
            resultObj.put(CommonJSONKey.BILLING_PKG_KEY, pricePkgArray);
        }

        //ios
        List<PricingPackageVO> oneTimePricePkgList =
                paymentManager.getOneTimePaymentPricingPackage(userVO.getMarketVO().getName());
        double price = 0;
        int month = 0;
        if (oneTimePricePkgList != null && oneTimePricePkgList.size() > 0) {
            JSONArray oneTimePricePckArray = new JSONArray();
            for (PricingPackageVO vo : oneTimePricePkgList) {
                oneTimePricePckArray.put(vo.toJSONObject());
                if (vo.getQuantity() > month) {
                    month = vo.getQuantity();
                    price = vo.getApplePrice();
                }
            }
            resultObj.put(CommonJSONKey.ONE_TIME_BILLING_PKG_KEY, oneTimePricePckArray);

            if (month > 0) {
                price = price / month;
                DecimalFormat df = new DecimalFormat("#0.00");
                if (Double.valueOf(df.format(price)) % 1.0 == 0) {
                    resultObj.put(CommonJSONKey.IOS_LOWEST_MONTHLY_PRICE, df.format(price - 0.01));
                } else {
                    resultObj.put(CommonJSONKey.IOS_LOWEST_MONTHLY_PRICE, df.format(price));
                }

            } else {
                resultObj.put(CommonJSONKey.IOS_LOWEST_MONTHLY_PRICE, "3.99");//hardcode a lowest price incase
            }
        }
    }

    /**
     * change password with tokenKey, sample {
     * "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084",
     * "currentPassword":"secqme",
     * "newPassword":"123456" }
     */
    @POST
    @Path("/updatePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updatePassword(String reqBody) {
        UserVO userVO = null;
        String message = null;
        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            String currentPassword = jobj.getString("currentPassword");
            String newPassword = jobj.getString(CommonJSONKey.USER_NEW_PASSWORD_KEY);
            userVO = userManager.getUserByActivationCode(authToken);
            userManager.updatePassword(userVO, currentPassword, newPassword);
            message = "{\"updatePassword\":\"ok\"}";
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return message;
    }

    /*
       {
         "emailAddr":"james.khoo@secq.me",    // or
         "mobileCountry":"MY",
         "mobileNumber":"162389788",
         "newPassword":"ABC123",
         "passwordResetPin":"001234"
       }
    */
    @POST
    @Path("/resetpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String resetPassword(String reqBody) {
        UserVO userVO = null;
        String message = null;
        try {
            JSONObject jobj = new JSONObject(reqBody);
            if (jobj.has(CommonJSONKey.EMAIL_ADDR_KEY)) {
                String emailAddress = jobj.getString(CommonJSONKey.EMAIL_ADDR_KEY);
                userVO = userManager.getUserByEmailAddress(emailAddress);
                if (userVO == null) {
                    throw new CoreException(ErrorType.USER_BYEMAIL_NOT_FOUND_ERROR, UserManager.USER_DEFAULT_LANGUAGE, emailAddress);
                }
            } else {
                String mobileCountryIso = jobj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
                String mobileNo = jobj.getString(CommonJSONKey.MOBILE_NO_KEY);
                userVO = userManager.getUserByMobileNumber(mobileCountryIso, mobileNo);
                if (userVO == null) {
                    throw new CoreException(ErrorType.USER_BYPHONE_NOT_FOUND_ERROR, UserManager.USER_DEFAULT_LANGUAGE, mobileCountryIso, mobileNo);
                }
            }
            userManager.updatePasswordWithResetPin(userVO, jobj.getString(CommonJSONKey.PASSWORD_RESET_PIN_KEY),
                    jobj.getString(CommonJSONKey.USER_NEW_PASSWORD_KEY));

            message = OK_STATUS;
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return message;
    }

    /**
     * send when user installed the app
     * { "userid":"1234@hotmal.com",
     * "clickId":"123leirjewio"}
     */
    @GET
    @Path("/appinstall/{campaignid}/{userid}/{androidid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String appInstall(@PathParam("campaignid") String campaignid,
                             @PathParam("userid") String userid, @PathParam("androidid") String androidid) {
        UserVO userVO = null;
        String message = null;
        myLog.debug("appinstall " + campaignid + " " + userid + " " + androidid);
        message = "{\"status\":\"ok\"}";
        return message;
    }

    /**
     * to get safety level array
     * { "authToken":"qwlejk-qwe-wqe-q",
     * "pictureStream":"123leirjewio"}
     */
    @POST
    @Path("/uploadprofilepicture")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadProfilePicture(String reqBody) {
        UserVO userVO = null;
        JSONObject resultObj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(reqBody);
            userVO = userManager.getUserByActivationCode(jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY));
            String encodedPic = jobj.getString(CommonJSONKey.PICTURE_STREAM_KEY);
            userManager.updateProfilePicture(userVO, encodedPic);
            resultObj.put(CommonJSONKey.PROFILE_PICTURE_URL_KEY, userVO.getProfilePictureURL());
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }
    
    private String getUserIDFromMobileNumbner(String countryISO, String mobileNumber) throws CoreException {
        CountryVO country = smsManager.getCountry(countryISO);
        return country.getCallingCode() + "-" + mobileNumber;
    }

    @POST
    @Path("/editusername")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String editUserName(String reqBody){
        UserVO userVO;
        JSONObject result = new JSONObject();
        try{
            JSONObject jobj = new JSONObject(reqBody);
            userVO = userManager.getUserByActivationCode(jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY));

            if (jobj.has(CommonJSONKey.USER_NAME_KEY)) {
                String name = jobj.getString(CommonJSONKey.USER_NAME_KEY);
                userManager.updateName(userVO, jobj.getString(CommonJSONKey.USER_NAME_KEY));
                result.put(CommonJSONKey.USER_NAME_KEY, name);
            } else {
                throw new CoreException(ErrorType.REQUIRE_FIELD_MISSING_ERROR, null);
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return result.toString();
    }

    @GET
    @Path("/analyticsids/{authToken}/{device}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getAnalyticsId(@PathParam("authToken") String authToken, @PathParam("device") String device) {
        JSONObject result = new JSONObject();

        try {
            result.put("device", device);
            result.put("id", userManager.getAnalyticsId(authToken, device));
        } catch (JSONException ex) {
            myLog.error("JSONException", ex);
        }

        return result.toString();
    }


    private void generateDeviceAnalyticsToken(UserVO userVO, JSONObject reqObj) {
        if (reqObj.has(CommonJSONKey.DEVICE_KEY)) {
            userVO.setDevice(reqObj.optString(CommonJSONKey.DEVICE_KEY));

            String device = userVO.getDevice().toLowerCase();
            String platformType;
            if (device.startsWith("android")) {
                platformType = "Android";
            } else if (device.equalsIgnoreCase("ios") || device.equalsIgnoreCase("iphone")) {
                platformType = "iOS";
            } else {
                platformType = "Unknown";
            }
            userManager.generateAnalyticsId(userVO, platformType);
        }
    }
}

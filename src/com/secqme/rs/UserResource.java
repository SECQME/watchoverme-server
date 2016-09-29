package com.secqme.rs;

import com.secqme.CoreException;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.promotion.PromotionVO;
import com.secqme.rs.v2.CommonJSONKey;
import com.secqme.sns.FacebookUtil;
import com.sun.jersey.api.ConflictException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.text.DecimalFormat;
import java.util.List;

@Path("/user")
public class UserResource extends BaseResource {

    public final static String VALID_ACCT_KEY = "validAcct";
    public final static String EMAIL_ADDR_KEY = "emailAddr";
    public final static String MOBILE_NO_KEY = "mobileNo";
    public final static String MOBILE_ACTIVE_PIN_KEY = "mobileActivePin";
    public final static String AUTH_TOKEN_KEY = "authToken";
    public final static String USER_TOKEN_KEY = "token";
    public final static String USER_ID_KEY = "userid";
    public final static String USER_NICK_NAME_KEY = "nickName";
    public static final String USER_NAME_KEY = "name";
    public final static String USER_PASSWORD_KEY = "password";
    public final static String DEVICE_KEY = "device";
    public final static String SUBSCRIPTION_PKG_KEY = "subscription";
    public final static String BILLING_PKG_KEY = "billingPackages";
    public final static String ONE_TIME_BILLING_PKG_KEY = "oneTimePackages";
    public final static String MAX_CONTACT_ALLOWS = "maxContactAllows";
    public final static String USER_LATEST_EVENT_KEY = "userLatestEvent";
    public final static String USER_SNS_CONFIG_KEY = "userSNSConfig";
    public final static String USER_IP_ADDRESS_KEY = "ipAddress"; //khlow20120621
    public final static String SNS_ACCESS_TOKEN = "snsAccessToken";
    public final static String USER_LOCATION_KEY = "location";
    public final static String LOCATION_LATITUDE_KEY = "lat";
    public final static String LOCATION_LONGITUDE_KEY = "lng";
    public final static String USER_COUNTRY_KEY = "country";
    public final static String SNS_NAME = "snsName";
    public final static String LANG_CODE_KEY = "langCode";
    public final static String STATUS = "status";
    public final static String MESSAGE_TOKEN_TYPE_KEY = "messageTokenType";
    public final static String PUSH_MESSAGE_TOKEN_KEY = "pushMessageToken";
    public final static String IOS_LOWEST_MONTHLY_PRICE = "iosLowestMonthlyPrice";
    public final static String USER_ID_TYPE_KEY = "userIDType";
    public final static String CLICK_ID_KEY = "clickId";
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
            verifyResultObj.put(VALID_ACCT_KEY, false);
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(EMAIL_ADDR_KEY)) {
                String emailAddr = jobj.getString(EMAIL_ADDR_KEY);
                userVO = userManager.getUserInfoByUserId(emailAddr);
            }
            String deviceName = null;
            if (jobj.has(DEVICE_KEY)) {
                deviceName = jobj.getString(DEVICE_KEY);
                myLog.debug("Device Name : " + deviceName);
            }
            userPassword = jobj.getString(USER_PASSWORD_KEY);
            UserVO newUser = userManager.authenticateUserByUserId(userVO.getUserid(), userPassword);
            if (newUser != null && newUser.getUserid().equalsIgnoreCase(userVO.getUserid())) {
                userVO.setPackageVO(newUser.getPackageVO());
                verifyResultObj.put(VALID_ACCT_KEY, true);
                verifyResultObj.put(USER_TOKEN_KEY, userVO.getActivationCode());
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
           {"emailAddr":"winson.tan@secq.me",
            "nickName":"Winson Tan"
            "snsName":"facebook",
            "snsAccessToken":"a73fafj2kfsa20", 
            "device":"android"}
        */

        myLog.debug("Verifying user request->" + reqBody);
        JSONObject verifyResultObj = new JSONObject();

        try {
            verifyResultObj.put(VALID_ACCT_KEY, false);
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;

            String emailAddr = jobj.getString(EMAIL_ADDR_KEY);
            String snsAccessToken = jobj.getString(SNS_ACCESS_TOKEN);
            String snsName = jobj.getString(SNS_NAME);

            // take the SNS accessToken validate against SNS userId
            boolean isSnsVerified = verifySnsAccessToken(emailAddr, snsName, snsAccessToken);

            if (isSnsVerified) {
                userVO = userManager.getUserInfoByUserId(emailAddr);
                if (userVO != null) {
                    // response with activationCode so that user can directly login
                    verifyResultObj.put(USER_TOKEN_KEY, userVO.getActivationCode());
                    verifyResultObj.put(VALID_ACCT_KEY, true);
                    verifyResultObj.put(USER_ID_KEY, userVO.getUserid());
                } else {
                    // response back so that user key in password for our server account
                    verifyResultObj.put(STATUS, "NEW_USER");
                }
            } else {
                throw new ConflictException("Social Network Login Failed: invalid access token");
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return verifyResultObj.toString();
    }

    /*
     * Take emailAddr verify against the email address return by SNS api by using the snsAccessToken
     * to make sure integrity between phone app and server
     */
    private boolean verifySnsAccessToken(String emailAddr, String snsName,
                                         String snsAccessToken) {

        if (FacebookUtil.FACEBOOK_NAME.equals(snsName)) {
            JSONObject json = fbService.getGraphByAccessToken(snsAccessToken);

            // compare the emailAddr with email return from facebook graph
            try {
                String emailFromSns = (String) json.get("email");

                if (emailAddr.equals(emailFromSns)) {
                    myLog.debug(emailFromSns + " is verified");
                    return true;
                } else {
                    myLog.debug("emailAddr " + emailAddr + " is not same with emailFromSns " + emailFromSns);
                }
            } catch (JSONException ex) {
                myLog.error("JSON error", ex);
            }
        }

        return false;
    }

    /*
      {
        "name":"James Khoo",
        "device":"android",
        "langCode":"en",
        "mobileCountry":"MY",
        "mobileNumber":"162389788",
        "location":{"lat":3.06766,"lng":101.62},
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
            String userName = reqObj.getString(USER_NAME_KEY);
            String userDevice = reqObj.getString(DEVICE_KEY);
            String userLanguage = reqObj.getString(LANG_CODE_KEY);

            if (reqObj.has(USER_LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = reqObj.getJSONObject(USER_LOCATION_KEY);
                latitude = locationObj.optDouble(LOCATION_LATITUDE_KEY, locationObj.getDouble(CommonJSONKey.LATITUDE_KEY));
                longitude = locationObj.optDouble(LOCATION_LONGITUDE_KEY, locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY));
            }

            userMobileCountry = reqObj.getString(UserVO.MOBILE_COUNTRY_KEY);
            userMobileNumber = reqObj.getString(UserVO.MOBILE_NO_KEY);
            UserVO userVO = userManager.registerTemporaryUser(userName,
                    userMobileCountry, userMobileNumber,
                    userLanguage, userDevice, latitude, longitude);
            resultObj.put(USER_TOKEN_KEY, userVO.getActivationCode());
            resultObj.put(USER_ID_KEY, userVO.getUserid());

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
            String authToken = reqObj.getString(AUTH_TOKEN_KEY);
            String userName = reqObj.getString(USER_NAME_KEY);
            String userPassword = reqObj.getString(USER_PASSWORD_KEY);
            String userEmailAddr = reqObj.has(EMAIL_ADDR_KEY) ? reqObj.getString(EMAIL_ADDR_KEY) : null;
            String userMobileCountry = reqObj.getString(UserVO.MOBILE_COUNTRY_KEY);
            String userMobileNumber = reqObj.getString(UserVO.MOBILE_NO_KEY);

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
       "location":{"lat":3.06766,"lng":101.62},
        "userSNSConfig":{    // Optional
             "snsUID":"570008013",
             "access_token":"XDFFG342335Q060WGZD",
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
                "gender":"male"
        }
     }
    */
    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String signUpUser(String reqBody) {
        String userMobileNumber;
        String userMobileCountry;
        JSONObject resultObj = new JSONObject();
        Double latitude = null;
        Double longitude = null;
        JSONObject userSNSConfigObj = null;
        try {
            JSONObject reqObj = new JSONObject(reqBody);

            String userName = reqObj.getString(USER_NAME_KEY);
            String userDevice = reqObj.getString(DEVICE_KEY);
            String userLanguage = reqObj.has(LANG_CODE_KEY) ? reqObj.getString(LANG_CODE_KEY) : null;
            String userPassword = reqObj.getString(USER_PASSWORD_KEY);
            String userEmailAddr = reqObj.has(EMAIL_ADDR_KEY) ? reqObj.getString(EMAIL_ADDR_KEY) : null;

            if (reqObj.has(USER_LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = reqObj.getJSONObject(USER_LOCATION_KEY);
                latitude = locationObj.optDouble(LOCATION_LATITUDE_KEY, locationObj.getDouble(CommonJSONKey.LATITUDE_KEY));
                longitude = locationObj.optDouble(LOCATION_LONGITUDE_KEY, locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY));
            }

            userMobileCountry = reqObj.getString(UserVO.MOBILE_COUNTRY_KEY);
            userMobileNumber = reqObj.getString(UserVO.MOBILE_NO_KEY);
            if(reqObj.has(USER_SNS_CONFIG_KEY)) {
                userSNSConfigObj = reqObj.getJSONObject(USER_SNS_CONFIG_KEY);
            }

            UserVO userVO = userManager.registerUser(userName, userPassword,
                    userEmailAddr, userMobileCountry, userMobileNumber, userLanguage, userDevice, latitude, longitude, userSNSConfigObj, 0, null);

            // Check if the pass in request have
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
            userVO.setUserid(jobj.getString(USER_ID_KEY));
            userVO.setNickName(jobj.getString(USER_NICK_NAME_KEY));
            userVO.setPassword(jobj.getString(USER_PASSWORD_KEY));
            String userCountryCode = null;

            if (jobj.has(USER_LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = jobj.getJSONObject(USER_LOCATION_KEY);
                Double latitude = locationObj.optDouble(LOCATION_LATITUDE_KEY, locationObj.getDouble(CommonJSONKey.LATITUDE_KEY));
                Double longitude = locationObj.optDouble(LOCATION_LONGITUDE_KEY, locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY));
                userCountryCode = locationUtil.getCountryCodeByLocation(latitude, longitude);
                userVO.setTimeZone(locationUtil.getTimeZone(latitude, longitude));
            }

            if (userCountryCode != null) {
                userVO.setCountryVO(smsManager.getCountry(userCountryCode));
                myLog.debug("Getting Country VO, code->" + userCountryCode);
            }

            if (jobj.has(MOBILE_NO_KEY)) {
                userVO.setMobileNo(jobj.getString(MOBILE_NO_KEY));
            }

            if (jobj.has(DEVICE_KEY)) {
                userVO.setDevice(jobj.getString(DEVICE_KEY));
            }

            if (jobj.has(LANG_CODE_KEY)) {
                userVO.setLangCode(jobj.getString(LANG_CODE_KEY));
            }

            userManager.registerUser(userVO.getNickName(), userVO.getPassword(), userVO.getEmailAddress(),
                    userCountryCode, userVO.getMobileNo(), userVO.getLangCode(), userVO.getDevice(),
                    userVO.getLatitude(), userVO.getLongitude(), jobj.optJSONObject(USER_SNS_CONFIG_KEY),
                    userVO.getClientVersion(), null);

            // Add default event list to user
            // Check if the pass in request have
            resultObj.put(USER_TOKEN_KEY, userVO.getActivationCode());
            resultObj.put(USER_ID_KEY, userVO.getUserid());
            if (userVO.getCountryVO() != null) {
                resultObj.put(USER_COUNTRY_KEY, userVO.getCountryVO().getIso());
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
     * @throws CoreException
     */
    @POST
    @Path("/activatelitepkg")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String activateLitePackage(String reqBody) throws CoreException {
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            String userToken = reqObj.getString(AUTH_TOKEN_KEY);
            String reqUserID = reqObj.getString(USER_ID_KEY);
            UserVO userVO = userManager.authenticateUserByActivationCode(userToken);
            billingManager.activateFreeMiumPackage(userVO, true);

        } catch (JSONException ex) {
            myLog.error("Error on activating LITE PACKAGE for user", ex);
        }
        return OK_STATUS;
    }

    /*
     * pre with tokenKey, and userid, sample
      {
        "userid":"james.khoo@secq.me",
        "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084",
        "ipAddress":"49.181.63.173",
        "location":{"lat":3.06766,"lng":101.62},
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
            Double latitude = null;
            Double longitude = null;
            JSONObject reqObj = new JSONObject(reqBody);
            String userToken = reqObj.getString(AUTH_TOKEN_KEY);
            String reqUserID = reqObj.getString(USER_ID_KEY);

            UserVO userVO = userManager.authenticateUserByActivationCode(userToken);

            if (reqObj.has(USER_LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = reqObj.getJSONObject(USER_LOCATION_KEY);
                if (locationObj.has(LOCATION_LATITUDE_KEY)) {
                    latitude = locationObj.getDouble(LOCATION_LATITUDE_KEY);
                    longitude = locationObj.getDouble(LOCATION_LONGITUDE_KEY);
                } else {
                    latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                    longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
                }

                userManager.updateLocation(userVO, latitude, longitude, true);
            }

            if (reqObj.has(PUSH_MESSAGE_TOKEN_KEY) && reqObj.has(MESSAGE_TOKEN_TYPE_KEY)) {
                userManager.updateUserPushMessageToken(userVO,
                        reqObj.getString(MESSAGE_TOKEN_TYPE_KEY), reqObj.getString(PUSH_MESSAGE_TOKEN_KEY));
            }

            prepareUserLoginInfo(resultObj, userVO);

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultObj.toString();
    }

    /*
       {
         "emailAddr":"james.khoo@secq.me",
         "mobileCountry":"MY",
         "mobileNumber":"162389788",
         "password":"mypassword"
       }

     */
    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String authenticateUser(String reqBody) throws CoreException {
        myLog.debug("Authenticate User:" + reqBody);
        JSONObject resultObj = new JSONObject();
        String userid = null;
        String userPassword;
        try {
            JSONObject reqObj = new JSONObject(reqBody);
            userPassword = reqObj.getString(USER_PASSWORD_KEY);
            if(reqObj.has(EMAIL_ADDR_KEY)) {
                userid = reqObj.getString(EMAIL_ADDR_KEY);
            } else {
                userid = getUserIDFromMobileNumbner(reqObj.getString(UserVO.MOBILE_COUNTRY_KEY),
                        reqObj.getString(UserVO.MOBILE_NO_KEY));
            }
            UserVO userVO = userManager.authenticateUserByUserId(userid, userPassword);
            prepareUserLoginInfo(resultObj, userVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultObj.toString();
    }


    private void prepareUserLoginInfo(JSONObject resultObj, UserVO userVO) throws JSONException, CoreException {
        SecqMeEventVO eventVO = eventManager.getUserLatestEvent(userVO.getUserid());
        if (eventVO != null) {
            resultObj.put(USER_LATEST_EVENT_KEY, eventVO.getJSON());
        }
        resultObj.put(USER_TOKEN_KEY, userVO.getActivationCode());
        resultObj.put(USER_ID_KEY, userVO.getUserid());
        resultObj.put(UserVO.USER_ID_KEY, userVO.getUserid());
        resultObj.put(BillingPkgVO.PACKAGE_NAME_KEY, userVO.getPackageVO().getPkgName());
        resultObj.put(BillingPkgVO.ACCESS_RIGHT, userVO.getPackageVO().getAccessRightJSON());
        resultObj.put(MAX_CONTACT_ALLOWS, userVO.getPackageVO().getMaxSMSContactAllow());
        BillingCycleVO billCycleVO = billingManager.getUserLatestBillCycleVO(userVO);

        if (billCycleVO != null) {
            if (billCycleVO.getEndDate() != null) {
                resultObj.put(BillingCycleVO.BILL_EXPIRED_AT_KEY, billCycleVO.getEndDate().getTime());
            }

            // REMARK Needs to put the object as a String,  as
            // Sometime user is assigned to billing package with unlimited credit
            //
            resultObj.put(BillingCycleVO.EVENT_CREDIT_BALANCE_KEY,
                    billCycleVO.getEventCreditBalanceText());
            resultObj.put(BillingCycleVO.SMS_CREDIT_BALANCE_KEY,
                    billCycleVO.getSMSCreditBalanceText());
            if (userVO.getUserCurrentSubscription() != null) {
                resultObj.put(SUBSCRIPTION_PKG_KEY, userVO.getUserCurrentSubscription().toJSON());
            } else {
                PromotionVO promotionVO = promotionManager.getUserCurrentPromotion(userVO);
                if (promotionVO != null) {
                    resultObj.put("promotion", promotionVO.toJSONObject());
                } else {
                    populateUserBillingPackageList(userVO, resultObj);
                }
            }


            if (userVO.getCountryVO() != null) {
                resultObj.put(UserVO.MOBILE_COUNTRY_KEY, userVO.getCountryVO().getIso());
                resultObj.put(USER_COUNTRY_KEY, userVO.getCountryVO().getIso());
            }
            if (userVO.getNickName() != null) {
                resultObj.put(UserVO.USER_NICK_NAME_KEY, userVO.getNickName());
            }

        }
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
            resultObj.put(BILLING_PKG_KEY, pricePkgArray);
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
            resultObj.put(ONE_TIME_BILLING_PKG_KEY, oneTimePricePckArray);

            if (month > 0) {
                price = price / month;
                DecimalFormat df = new DecimalFormat("#0.00");
                if (Double.valueOf(df.format(price)) % 1.0 == 0) {
                    resultObj.put(IOS_LOWEST_MONTHLY_PRICE, df.format(price - 0.01));
                } else {
                    resultObj.put(IOS_LOWEST_MONTHLY_PRICE, df.format(price));
                }

            } else {
                resultObj.put(IOS_LOWEST_MONTHLY_PRICE, "3.99");//hardcode a lowest price incase
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
            String authToken = jobj.getString(AUTH_TOKEN_KEY);
            String currentPassword = jobj.getString("currentPassword");
            String newPassword = jobj.getString("newPassword");
            userVO = userManager.getUserByActivationCode(authToken);
            userManager.updatePassword(userVO, currentPassword, newPassword);
            message = "{\"updatePassword\":\"ok\"}";

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return message;
    }

    /**
     * reset password with tokenKey and email, sample
     * TODO needs to
     * { "emailAddr":"1234@hotmal.com" }
     */
    @POST
    @Path("/resetPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String resetPassword(String reqBody) {
        UserVO userVO = null;
        String message = null;
        try {
            JSONObject jobj = new JSONObject(reqBody);
            String emailAddr = jobj.getString(EMAIL_ADDR_KEY);
            userVO = userManager.getUserInfoByUserId(emailAddr);
            userManager.resetPasswordWithRandomString(userVO);
            message = "{\"status\":\"ok\"}";

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return message;
    }

    /**
     * send when user installed the app
     * { "userid":"1234@hotmal.com",
     *   "clickId":"123leirjewio"}
     */
    @GET
    @Path("/appinstall/{campaignid}/{userid}/{androidid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String appInstall(@PathParam("campaignid") String campaignid,
    		@PathParam("userid") String userid, @PathParam("androidid") String androidid) {
        UserVO userVO = null;
        String message = null;
//        try {
        	myLog.debug("appinstall " + campaignid + " " + userid + " " + androidid);
//            JSONObject jobj = new JSONObject(reqBody);
            message = "{\"status\":\"ok\"}";
//        } catch (CoreException e) {
//            message = e.getMessageAsJSON().toString();
//        } catch (JSONException e) {
//            throw new ConflictException("Invalid request Body->" + e.getMessage());
//        }
        return message;
    }
    
    private String getUserIDFromMobileNumbner(String countryISO, String mobileNumber) throws CoreException {
        CountryVO country = smsManager.getCountry(countryISO);
        return country.getCallingCode() + "-" + mobileNumber;
    }

}

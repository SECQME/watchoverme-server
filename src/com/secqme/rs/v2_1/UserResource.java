package com.secqme.rs.v2_1;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.event.FullEventInfoVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.manager.UserManager;
import com.secqme.rs.BaseResource;
import com.secqme.rs.v2.CommonJSONKey;
import com.secqme.util.marketing.MarketingCampaign;
import com.secqme.util.notification.sns.SNSStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;

/**
 * Created by edward on 12/08/2015.
 */
@Path("/v2.1/users")
public class UserResource extends BaseResource {
    private final static Logger myLog = Logger.getLogger(UserResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createUser(@Context HttpServletRequest httpServletRequest, JSONObject reqObj) {
        String userMobileNumber;
        String userMobileCountry;
        JSONObject resultObj = new JSONObject();
        Double latitude = null;
        Double longitude = null;
        JSONObject userSNSConfigObj = null;
        double clientVersion = 0;

        String pictureStream = null;
        try {
            String userName = reqObj.getString(CommonJSONKey.USER_NAME_KEY);
            String userDevice = getClientOS(httpServletRequest, reqObj);
            String userLanguage = reqObj.has(CommonJSONKey.LANG_CODE_KEY) ? reqObj.getString(CommonJSONKey.LANG_CODE_KEY) : null;
            String userPassword = reqObj.getString(CommonJSONKey.USER_PASSWORD_KEY);
            String userEmailAddr = reqObj.optString(CommonJSONKey.EMAIL_ADDR_KEY, null);

            if (reqObj.has(CommonJSONKey.LOCATION_KEY)) { //i.e fail to get location from request ip
                // then check if user has given location object;
                JSONObject locationObj = reqObj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
            }

            userMobileCountry = reqObj.optString(CommonJSONKey.MOBILE_COUNTRY_KEY, null);
            userMobileNumber = reqObj.optString(CommonJSONKey.MOBILE_NO_KEY, null);
            userSNSConfigObj = reqObj.optJSONObject(CommonJSONKey.USER_SNS_CONFIG_KEY);
            clientVersion = getClientVersion(httpServletRequest, reqObj);

            pictureStream = reqObj.optString(CommonJSONKey.PICTURE_STREAM_KEY, null);

            UserVO userVO = userManager.registerUser(userName, userPassword,
                    userEmailAddr, userMobileCountry, userMobileNumber, userLanguage, userDevice,
                    latitude, longitude, userSNSConfigObj, clientVersion, pictureStream);

            if (reqObj.has(CommonJSONKey.CLIENT_VERSION_KEY)) {
                userManager.checkContactAddedList(userVO);
            }
            if (userVO.getContactList() != null) {
                myLog.debug("new user contactList size1 :" + userVO.getContactList().size());
            }
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            if (reqObj.has(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY) && reqObj.has(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY)) {
                userManager.updateUserPushMessageToken(userVO,
                        reqObj.getString(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY), reqObj.getString(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY));
            }
            if (userVO.getContactList() != null) {
                myLog.debug("new user contactList size2 :" + userVO.getContactList().size());
            }
            //check if user already has been added by someone


            String clientId = httpServletRequest.getHeader("X-Client-Id");
            if (clientId != null && clientId.equalsIgnoreCase("walky")) {
                userManager.markWalkyUser(userVO);
            }

            // Check if the pass in request have
            prepareUserInfo(resultObj, userVO);

            // Remove from Emergency Contact funnel
            userManager.deleteMarketingEmailContact(userVO, MarketingCampaign.EMERGENCY_CONTACT_FUNNEL);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login(@Context HttpServletRequest httpServletRequest, JSONObject reqObj) throws CoreException {
        JSONObject resultObj = new JSONObject();
        String userid;
        String userPassword;
        UserVO userVO;
        try {
            userPassword = reqObj.getString(CommonJSONKey.USER_PASSWORD_KEY);
            if (reqObj.has(CommonJSONKey.EMAIL_ADDR_KEY)) {
                userid = reqObj.getString(CommonJSONKey.EMAIL_ADDR_KEY);
                myLog.debug("Login user via email - " + userid);
                userVO = userManager.authenticateUserByUserId(userid, userPassword);
            } else {
                myLog.debug("Login user via mobile - " + reqObj.getString(UserVO.MOBILE_COUNTRY_KEY)
                        + "-" + reqObj.getString(UserVO.MOBILE_NO_KEY));
                userVO = userManager.authenticateUserByUserId(userManager.getUserIdFromMobileNumber(reqObj.getString(UserVO.MOBILE_COUNTRY_KEY), reqObj.getString(UserVO.MOBILE_NO_KEY)), userPassword);
            }

            Double clientVersion = getClientVersion(httpServletRequest, reqObj);
            if (clientVersion != null) {
                userManager.checkForClientVersionUpgrade(userVO, clientVersion);
            }

            if (reqObj.has(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY) && reqObj.has(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY)) {
                userManager.updateUserPushMessageToken(userVO,
                        reqObj.getString(CommonJSONKey.MESSAGE_TOKEN_TYPE_KEY), reqObj.getString(CommonJSONKey.PUSH_MESSAGE_TOKEN_KEY));
            }

            resultObj.put(CommonJSONKey.ID_KEY, userVO.getId());
            resultObj.put(CommonJSONKey.AUTH_TOKEN_KEY, userVO.getActivationCode());
            resultObj.put(CommonJSONKey.USER_ID_KEY, userVO.getUserid());

            String clientId = httpServletRequest.getHeader("X-Client-Id");
            if (clientId != null && clientId.equalsIgnoreCase("walky")) {
                userManager.markWalkyUser(userVO);
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultObj.toString();
    }

    @POST
    @Path("/login/sns/{providerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject loginViaSNS(@Context HttpServletRequest httpServletRequest, @PathParam("providerId") String providerId, JSONObject reqObj) throws CoreException {
        myLog.debug("Login via SNS: " + providerId);

        JSONObject resultObj = new JSONObject();
        try {
            UserSNSConfigVO userSNSConfigVO = snsManager.generateSNSConfig(providerId, null, reqObj.getJSONObject(CommonJSONKey.SNS_ADDITONAL_CONFIG_KEY));

            if (snsManager.isRegistered(userSNSConfigVO)) {
                UserVO userVO = userManager.authenticateUserBySNS(userSNSConfigVO.getSnsName(), userSNSConfigVO.getSnsuid());
                resultObj.put(CommonJSONKey.STATUS, SNSStatus.REGISTERED);
                prepareAuthInfo(resultObj, userVO);

                String clientId = httpServletRequest.getHeader("X-Client-Id");
                if (clientId != null && clientId.equalsIgnoreCase("walky")) {
                    userManager.markWalkyUser(userVO);
                }
            } else {
                UserVO userVO = snsManager.findAllegedlyUser(userSNSConfigVO);
                if (userVO != null) {
                    resultObj.put(CommonJSONKey.STATUS, SNSStatus.ALLEGEDLY_REGISTERED);
                    resultObj.put(CommonJSONKey.USER_ID_TYPE_KEY, userVO.getUserIdType());
                } else {
                    userVO = userManager.registerUser(userSNSConfigVO, getClientOS(httpServletRequest, reqObj));
                    resultObj.put(CommonJSONKey.STATUS, SNSStatus.ACCOUNT_CREATED);

                    String clientId = httpServletRequest.getHeader("X-Client-Id");
                    if (clientId != null && clientId.equalsIgnoreCase("walky")) {
                        userManager.markWalkyUser(userVO);
                    }

                    prepareAuthInfo(resultObj, userVO);
                }
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultObj;
    }

    @PUT
    @Path("/me")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject updateUser(@Context HttpServletRequest httpServletRequest, JSONObject reqObj) throws CoreException {
        myLog.debug("Getting current user detail, req->" + reqObj);
        JSONObject resultObj = new JSONObject();
        try {
            String userToken = getAuthorizationToken(httpServletRequest, reqObj);
            UserVO userVO = userManager.authenticateUserByActivationCode(userToken);

            if (reqObj.has(CommonJSONKey.USER_NAME_KEY)) {
                userManager.updateName(userVO, reqObj.getString(CommonJSONKey.USER_NAME_KEY));
            }

            if (reqObj.has(CommonJSONKey.PICTURE_STREAM_KEY)) {
                String encodedPic = reqObj.getString(CommonJSONKey.PICTURE_STREAM_KEY);
                userManager.updateProfilePicture(userVO, encodedPic);
            }

            Double clientVersion = getClientVersion(httpServletRequest, reqObj);
            if (clientVersion != null) {
                userManager.checkForClientVersionUpgrade(userVO, clientVersion);
            }

            if (reqObj.has(CommonJSONKey.LOCATION_KEY)) {
                JSONObject locationObj = reqObj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                double latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                double longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
                userManager.updateLocation(userVO, latitude, longitude, true);
            }

            if (reqObj.has(CommonJSONKey.MOBILE_COUNTRY_KEY) && reqObj.has(CommonJSONKey.MOBILE_NO_KEY)) {
                userManager.updateMobileNumber(userVO, reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY), reqObj.getString(CommonJSONKey.MOBILE_NO_KEY));
            }

            if (reqObj.has(CommonJSONKey.EMAIL_ADDR_KEY)) {
                userManager.updateEmailAddress(userVO, reqObj.getString(CommonJSONKey.EMAIL_ADDR_KEY));
            }

            String os = getClientOS(httpServletRequest, reqObj);
            if (os != null) {
                userManager.generateAnalyticsId(userVO, os);
            }

            String clientId = httpServletRequest.getHeader("X-Client-Id");
            if (clientId != null && clientId.equalsIgnoreCase("walky")) {
                userManager.markWalkyUser(userVO);
            }

            prepareUserInfo(resultObj, userVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultObj;
    }

    @POST
    @Path("/me/sns/{providerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject integrateSNSAccount(@Context HttpServletRequest httpServletRequest, @PathParam("providerId") String providerId, JSONObject reqObj) {
        try {
            String userToken = getAuthorizationToken(httpServletRequest, reqObj);
            UserVO userVO = userManager.getUserByActivationCode(userToken);

            UserSNSConfigVO configVO = snsManager.generateSNSConfig(providerId, null, reqObj.getJSONObject(CommonJSONKey.SNS_ADDITONAL_CONFIG_KEY));
            configVO.setUserVO(userVO);
            configVO.setNotify(reqObj.optBoolean(CommonJSONKey.NOTIFY, false));

            userManager.addReplaceSNSConfig(userVO, configVO);

            return configVO.toJSONObj();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
    }

    @PUT
    @Path("/me/sns/{providerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject updateSNSAccount(@Context HttpServletRequest httpServletRequest, @PathParam("providerId") String providerId, JSONObject reqObj) {
        try {
            String userToken = getAuthorizationToken(httpServletRequest, reqObj);
            UserVO userVO = userManager.getUserByActivationCode(userToken);

            for(UserSNSConfigVO userSNSConfigVO : userVO.getSnsConfigList()) {
                if (StringUtils.equalsIgnoreCase(providerId, userSNSConfigVO.getSnsName())) {
                    userSNSConfigVO.setNotify(reqObj.getBoolean(CommonJSONKey.NOTIFY));
                    userManager.updateUser(userVO);
                    return userSNSConfigVO.toJSONObj();
                }
            }

            throw new CoreException(ErrorType.USER_SNS_NOT_FOUND, UserManager.USER_DEFAULT_LANGUAGE, providerId);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
    }

    @DELETE
    @Path("/me/sns/{providerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String disintegrateSNSAccount(@Context HttpServletRequest httpServletRequest, @PathParam("providerId") String providerId) {
        try {
            String userToken = getAuthorizationToken(httpServletRequest, null);
            UserVO userVO = userManager.getUserByActivationCode(userToken);

            userManager.removeSNSConfig(userVO, providerId);

            return BaseResource.OK_STATUS;
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
    }

    @POST
    @Path("/me/verify/email")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String verifyEmailAddress(@Context HttpServletRequest httpServletRequest) {
        try {
            String userToken = getAuthorizationToken(httpServletRequest, null);
            UserVO userVO = userManager.authenticateUserByActivationCode(userToken);

            userManager.sendEmailVerification(userVO);
            return BaseResource.OK_STATUS;
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
    }

    @POST
    @Path("/forgotpassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject requestResetPassword(JSONObject reqObj) throws CoreException {
        try {
            String userid;
            UserVO userVO = null;
            if (reqObj.has(CommonJSONKey.EMAIL_ADDR_KEY)) {
                userid = reqObj.getString(CommonJSONKey.EMAIL_ADDR_KEY);
                myLog.debug("Forgot password via email - " + userid);
                userVO = userManager.getUserInfoByUserId(userid);
            } else {
                userid = userManager.getUserIdFromMobileNumber(reqObj.getString(UserVO.MOBILE_COUNTRY_KEY), reqObj.getString(UserVO.MOBILE_NO_KEY));
                myLog.debug("Forgot password via mobile - " + userid);
                userVO = userManager.getUserInfoByUserId(userid);
            }

            if (userVO != null) {
                if (userVO.getEmailAddress() != null) {
                    userManager.forgotPasswordUsingToken(userVO);

                    JSONObject resultObj = new JSONObject();
                    resultObj.put(CommonJSONKey.STATUS, "ok");
                    resultObj.put(CommonJSONKey.EMAIL_ADDR_KEY, userVO.getEmailAddress());
                    return resultObj;
                } else {
                    throw new CoreException(ErrorType.USER_NO_EMAIL_ERROR, userVO.getLangCode());
                }
            } else {
                throw new CoreException(ErrorType.USER_BYID_NOT_FOUND_ERROR, UserManager.USER_DEFAULT_LANGUAGE);
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
    }

    private void prepareAuthInfo(JSONObject resultObj, UserVO userVO) throws JSONException {
        resultObj.put(CommonJSONKey.ID_KEY, userVO.getId());
        resultObj.put(CommonJSONKey.AUTH_TOKEN_KEY, userVO.getActivationCode());
        resultObj.put(CommonJSONKey.USER_ID_KEY, userVO.getUserid());
        resultObj.put(CommonJSONKey.USER_ID_TYPE_KEY, userVO.getUserIdType());
    }

    private void prepareUserInfo(JSONObject resultObj, UserVO userVO) throws JSONException, CoreException {
        prepareAuthInfo(resultObj, userVO);

        SecqMeEventVO eventVO = eventManager.getUserLatestEvent(userVO.getUserid());
        if (eventVO != null && eventVO.isEventRunning()) {
            FullEventInfoVO fullEventInfoVO = eventManager.getFullEventInfo(userVO, eventVO.getId());
            resultObj.put(CommonJSONKey.USER_LATEST_EVENT_KEY, fullEventInfoVO.toJSON());
        }

        resultObj.put(CommonJSONKey.EMAIL_ADDR_KEY, userVO.getEmailAddress());
        resultObj.put(BillingPkgVO.PACKAGE_NAME_KEY, userVO.getPackageVO().getPkgName());
        resultObj.put(CommonJSONKey.BILL_PKG_TYPE_KEY, userVO.getPackageVO().getPkgType().toString());
        resultObj.put(CommonJSONKey.TOTAL_EVENT_REGISTERED_KEY, eventManager.getUserTotalRegisteredEvent(userVO.getUserid()));

        if (userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
            List<UserSNSConfigVO> userSNSConfigVOs = userVO.getSnsConfigList();
            JSONArray userSNSConfigs = new JSONArray();

            for (UserSNSConfigVO snsConfigVO : userVO.getSnsConfigList()) {
                userSNSConfigs.put(snsConfigVO.toJSONObj());
            }

            resultObj.put(CommonJSONKey.USER_SNS_CONFIGS_KEY, userSNSConfigs);
        }

        JSONArray contactJSONArray = null;
        if (userVO.getContactList() != null && userVO.getContactList().size() > 0) {
            contactJSONArray = parseUserContactListAsJSONArray(userVO);
            resultObj.put(CommonJSONKey.CONTACT_LIST_KEY, contactJSONArray);
        }

        // contact user that has event running
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
            resultObj.put(BillingCycleVO.SMS_CREDIT_BALANCE_KEY,
                    billCycleVO.getSMSCreditBalanceText());

            if (userVO.getCountryVO() != null) {
                resultObj.put(CommonJSONKey.USER_COUNTRY_KEY, userVO.getCountryVO().getIso());
            } else {
                // HACK: If the country is null, we return the mobile country.
                // We do this because of iOS client app v6.21 error
                if (userVO.getMobileCountry() != null) {
                    resultObj.put(CommonJSONKey.USER_COUNTRY_KEY, userVO.getMobileCountry().getIso());
                }
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

        // safety level percentage
        populateUserSafetyLevelList(userVO, resultObj);
        // profile pic url
        resultObj.put(CommonJSONKey.PROFILE_PICTURE_URL_KEY, userVO.getProfilePictureURL());
        resultObj.put(CommonJSONKey.ANALYTICS_IDS_KEY, userVO.getAnalyticsIds());

        if (userVO.getEmailVerificationDate() != null) {
            resultObj.put(CommonJSONKey.EMAIL_VERIFIED_AT, userVO.getEmailVerificationDate().getTime());
        }
    }
}
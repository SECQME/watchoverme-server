package com.secqme.rs;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.sms.SmsCountryVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Path("/contact")
public class ContactResource extends BaseResource {

    private final static String CONTACT_LIST_KEY = "contactList";
    private final static String SNS_LIST_KEY  = "snsList";
    private final static String SUPPORTED_COUNTRY_LIST_KEY = "supportedCountryList";
    private final static Logger myLog = Logger.getLogger(ContactResource.class);

    public ContactResource() {
    }
    // usertoken = "0416f2af-2a58-401b-8a29-f19c90f2f084"
    /*
     Example : 
      { "contactList":[ 
             {"contactId":1,"mobileCountryCode":"60","nickName":"James Khoo","emailNotifySafety":true,"emailNotifyEmergency":true,"emailAddr":"james.khoo@secq.me","mobileCountry":"MY","smsNotifyEmergency":true,"mobileNo":"162389788","smsNotifySafety":true},
             {"contactId":19,"mobileCountryCode":"60","nickName":"Lau Po","emailNotifySafety":true,"emailNotifyEmergency":true,"emailAddr":"Winnie.WL.Wong@sc.com","mobileCountry":"MY","smsNotifyEmergency":true,"mobileNo":"172002173","smsNotifySafety":false},
             {"contactId":174,"nickName":"Darling","emailNotifySafety":true,"emailNotifyEmergency":true,"emailAddr":"khoo.james@gmail.com","smsNotifyEmergency":false,"smsNotifySafety":false}],
        "supportedCountryList":[
                {"countryName":"Malaysia","countryCode":"60","country":"MY"},
                {"countryName":"Australia","countryCode":"61","country":"AU"},
                {"countryName":"Belgium","countryCode":"32","country":"BE"}],
        "snsList":[
             {"snsName":"facebook_dev","notify":true,"snsUID":"100000976508107"},
             {"snsName":"twitter","notify":true,"snsUID":"132431018"}]
    
     }*/
    @GET
    @Path("/{usertoken}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserContacts(@PathParam("usertoken") String userToken) {
        String resultStr = null;
        myLog.debug("Attemp to get user's Contact via token->" + userToken);
        try {
            JSONObject resultObject = new JSONObject();

            // Populate all the country as JSONArray
            JSONArray smsCountryArray = new JSONArray();
            for (SmsCountryVO countryVO : smsManager.getSupportedSMSCountryList()) {
                smsCountryArray.put(countryVO.toJSONObject());
            }
            resultObject.put(SUPPORTED_COUNTRY_LIST_KEY, smsCountryArray);


            UserVO userVO = userManager.getUserByActivationCode(userToken);
            if(userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
                JSONArray snsArray = new JSONArray();
                for(UserSNSConfigVO configVO : userVO.getSnsConfigList()) {
                    snsArray.put(configVO.toJSONObj());
                }
                resultObject.put(SNS_LIST_KEY, snsArray);
            }
            resultObject.put(CONTACT_LIST_KEY, getUserContactListAsJSONArray(userVO));
            resultStr = resultObject.toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;

    }
    
    @POST
    @Path("/sns/{usertoken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewSNS(@PathParam("usertoken") String userToken, String reqBody) {
        String result = "";
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            UserSNSConfigVO configVO = new UserSNSConfigVO(new JSONObject(reqBody));
            configVO.setUpdatedDate(new Date());
            configVO.setUserVO(userVO);
            if(configVO.getSnsName().startsWith("facebook")) {
                configVO.setSocialNetworkVO(fbUtil.getFbSNSVO());
            }
            userManager.addReplaceSNSConfig(userVO, configVO);
            result = OK_STATUS;
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        
        return result;
    }
    
    /*
     {
      "snsList" : 
         [ {"snsName":"facebook","notify":true},
           {"snsName":"twitter", "notify":false} ]
     */
    @PUT
    @Path("/sns/{usertoken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateUserSNSSetting(@PathParam("usertoken") String userToken, String reqBody) {
        String resultStr = null;
        myLog.debug("Attempt to get update user SNS Setting->" + reqBody);
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            JSONObject reqObj  = new JSONObject(reqBody);
            JSONArray snsArray = reqObj.getJSONArray(SNS_LIST_KEY);
            for(int i=0; i < snsArray.length(); i++) {
                UserSNSConfigVO tmpSNSConfigVO = new UserSNSConfigVO(snsArray.getJSONObject(i));
                for(UserSNSConfigVO userSNSConfigVO : userVO.getSnsConfigList()) {
                    String snsName = userSNSConfigVO.getSnsName().substring(0,5);
                    if(tmpSNSConfigVO.getSnsName().startsWith(snsName)) {
                        userSNSConfigVO.setNotify(tmpSNSConfigVO.isNotify());
                    }
                }
            }
            userManager.trimNameAndMobileNo(userVO);
            resultStr = OK_STATUS;
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }
    
    

    /*
    {  
    "contactId":1,
    "nickName":"James Khoo",
    "emailAddr":"james.khoo@secq.me",
    "mobileCountry":"MY",
    "mobileNo":"162389788",
    "emailNotifySafety":true,
    "emailNotifyEmergency":true,
    "smsNotifyEmergency":true,
    "smsNotifySafety":true
    }
     */
    @PUT
    @Path("/{usertoken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateUserContact(@PathParam("usertoken") String userToken,
            String reqBody) {
        String resultStr = null;
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            ContactVO contactVO = parseContactVO(userVO, reqBody);
            myLog.debug("replacing contact for user:" + userVO.getUserid() + ", contactid:" + contactVO.getId());
            contactManager.changeContact(userVO, contactVO);
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            resultObject.put(CONTACT_LIST_KEY, getUserContactListAsJSONArray(userVO));
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }

    @DELETE
    @Path("/{usertoken}/{contactid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUserContact(@PathParam("usertoken") String userToken,
            @PathParam("contactid") Long contactid) {
        String resultStr = null;
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            contactManager.removeContact(userVO, contactid);
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            resultObject.put(CONTACT_LIST_KEY, getUserContactListAsJSONArray(userVO));
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }

    /*
    Example 1
    {      
    "nickName":"James Khoo",
    "emailAddr":"james.khoo@secq.me",
    "mobileCountry":"MY",
    "mobileNo":"162389788",
    "emailNotifySafety":true,
    "emailNotifyEmergency":true,
    "smsNotifyEmergency":true,
    "smsNotifySafety":true,
    "relationship":"parent"
    }
    
    Example 2 -- without Mobile Number
    {      
    "nickName":"James Khoo",
    "emailAddr":"developer@secq.me",
    "emailNotifySafety":true,
    "emailNotifyEmergency":false,
    }
    
    Example 3  -- without Email Address
    {      
    "nickName":"Ah Beng",
    "mobileCountry":"MY",
    "mobileNo":"162389788",
    "smsNotifyEmergency":false,
    "smsNotifySafety":true
    }
     */
    @POST
    @Path("/{usertoken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewContact(@PathParam("usertoken") String userToken, String reqBody) {
        String resultStr = null;
        myLog.debug("Attempt to add new contact for user:->" + userToken + ", reqBody->" + reqBody);
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            ContactVO contactVO = parseContactVO(userVO, reqBody);
            contactManager.addNewContact(userVO, contactVO);
            // To do a refresh of user inorder to all new contact;
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            JSONObject resultObj = new JSONObject();
            resultObj.put(CONTACT_LIST_KEY, getUserContactListAsJSONArray(userVO));
            resultStr = resultObj.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;
    }

    private JSONArray getUserContactListAsJSONArray(UserVO userVO) throws JSONException {
        JSONArray contactArray = new JSONArray();
        List<ContactVO> userContactList = userVO.getContactList();
        if (userContactList != null && userContactList.size() > 0) {
            Collections.sort(userContactList);
            for (ContactVO contactVO : userContactList) {
                contactArray.put(contactVO.getJSONObject());
            }
        }
        return contactArray;
    }

    private ContactVO parseContactVO(UserVO userVO, String contactVOStr) throws JSONException {
        JSONObject contactObj = new JSONObject(contactVOStr);
        ContactVO contactVO = new ContactVO();
        contactVO.setNickName(contactObj.getString(ContactVO.NICK_NAME_KEY));
        contactVO.setUserVO(userVO);

        if (contactObj.has(ContactVO.CONTACT_ID_KEY)) {
            contactVO.setId(contactObj.getLong(ContactVO.CONTACT_ID_KEY));
        }

        if (contactObj.has(ContactVO.EMAIL_ADDR_KEY)) {
            contactVO.setEmailAddress(contactObj.getString(ContactVO.EMAIL_ADDR_KEY));
        }

        if (contactObj.has(ContactVO.EMAIL_EMERGENCY_KEY)) {
            contactVO.setNotifyEmail(contactObj.getBoolean(ContactVO.EMAIL_EMERGENCY_KEY));
        }

        if (contactObj.has(ContactVO.EMAIL_SAFETY_KEY)) {
            contactVO.setSafetyNotifyEmail(contactObj.getBoolean(ContactVO.EMAIL_SAFETY_KEY));
        }

        if (contactObj.has(ContactVO.MOBILE_COUNTRY_KEY)) {

            CountryVO countryVO = smsManager.getCountry(contactObj.getString(ContactVO.MOBILE_COUNTRY_KEY));
            contactVO.setCountryVO(countryVO);
        }

        if (contactObj.has(ContactVO.MOBILE_NO_KEY)) {
            String mobileNumber = contactObj.getString(ContactVO.MOBILE_NO_KEY);
            mobileNumber = mobileNumber.replace("-", "").replace("(","").replace(")","").replace(" ", "");

            contactVO.setMobileNo(mobileNumber);
        }

        if (contactObj.has(ContactVO.SMS_EMERGENCY_KEY)) {
            contactVO.setNotifySMS(contactObj.getBoolean(ContactVO.SMS_EMERGENCY_KEY));
        }

        if (contactObj.has(ContactVO.SMS_SAFETY_KEY)) {
            contactVO.setSafetyNotifySMS(contactObj.getBoolean(ContactVO.SMS_SAFETY_KEY));
        }
        
        if (contactObj.has(ContactVO.RELATIONSHIP_KEY)) {
            contactVO.setRelationship(contactObj.getString(ContactVO.RELATIONSHIP_KEY));
        }
        return contactVO;
    }
}

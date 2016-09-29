package com.secqme.rs.v2;

import java.util.ArrayList;
import java.util.List;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/v2/contact")
public class ContactResource extends BaseResource {

    private final static String TAG = "ContactResource";
    private final static Logger myLog = Logger.getLogger(ContactResource.class);

    public ContactResource() {
    }
    // usertoken = "0416f2af-2a58-401b-8a29-f19c90f2f084"
    /*
     Example : 
      { "contactList":[ 
             {"contactId":1,"mobileCountryCode":"60",
             "name":"James Khoo","emailAddr":"james.khoo@secq.me","mobileCountry":"MY","mobileNo":"162389788"},
        "snsList":[
             {"snsName":"facebook_dev","notify":true,"snsUID":"100000976508107"},
             {"snsName":"twitter","notify":true,"snsUID":"132431018"}]
    
     }*/
    @GET
    @Path("/{authToken}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserContacts(@PathParam("authToken") String userToken) {
        String resultStr = null;
        myLog.debug("Attempt to get user's Contact via token->" + userToken);
        try {
            JSONObject resultObject = new JSONObject();

            UserVO userVO = userManager.getUserByActivationCode(userToken);
            if(userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
                JSONArray snsArray = new JSONArray();
                for(UserSNSConfigVO configVO : userVO.getSnsConfigList()) {
                    snsArray.put(configVO.toJSONObj());
                }
                resultObject.put(CommonJSONKey.SNS_LIST_KEY, snsArray);
            }
            resultObject.put(CommonJSONKey.CONTACT_LIST_KEY, parseUserContactListAsJSONArray(userVO));
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;

    }
    

    
    /*
     {
      "snsList" : 
         [ {"snsName":"facebook","notify":true},
           {"snsName":"twitter", "notify":false} ]
     */
    @PUT
    @Path("/sns/{authToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateUserSNSSetting(@PathParam("authToken") String userToken, String reqBody) {
        String resultStr = null;
        myLog.debug("Attempt to get update user SNS Setting->" + reqBody);
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            JSONObject reqObj  = new JSONObject(reqBody);
            JSONArray snsArray = reqObj.getJSONArray(CommonJSONKey.SNS_LIST_KEY);
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
    }
     */
    @PUT
    @Path("/{userToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateUserContact(@PathParam("userToken") String userToken,
            String reqBody) {
        String resultStr = null;
        try {
        	JSONObject reqObject = new JSONObject(reqBody);
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            ContactVO contactVO = parseContactVO(userVO, reqBody);
            myLog.debug("replacing contact for user:" + userVO.getUserid() + ", contact ID:" + contactVO.getId());

            contactManager.changeContact(userVO, contactVO);
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            resultObject.put(CommonJSONKey.CONTACT_LIST_KEY, parseUserContactListAsJSONArray(userVO));
            populateUserSafetyLevelList(userVO, resultObject);
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }

    @DELETE
    @Path("/{userToken}/{contactID}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteUserContact(@PathParam("userToken") String userToken,
            @PathParam("contactID") Long contactID) {
        String resultStr = null;
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            contactManager.removeContact(userVO, contactID);
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            resultObject.put(CommonJSONKey.CONTACT_LIST_KEY, parseUserContactListAsJSONArray(userVO));
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
    public String deleteMultipleUserContact(@PathParam("userToken") String userToken,
    		String reqBody) {
        String resultStr = null;
        myLog.debug("Attempt to delete multiple contacts for user:->" + userToken + ", reqBody->" + reqBody);
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            JSONObject reqObject = new JSONObject(reqBody);
            JSONArray contactIdArray = reqObject.getJSONArray(CommonJSONKey.CONTACT_ID_LIST_KEY);
            
            if(contactIdArray.length() > 0) {
            	List<Long> contactIdList = new ArrayList<Long>();
            	for(int i=0;i<contactIdArray.length();i++) {
            		contactIdList.add(contactIdArray.getLong(i));
            	}
            	contactManager.removeContacts(userVO, contactIdList);
                userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            }
            resultObject.put(CommonJSONKey.CONTACT_LIST_KEY, parseUserContactListAsJSONArray(userVO));
            populateUserSafetyLevelList(userVO, resultObject);
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }
    /*
    Example 1
    {      
    "name":"James Khoo",
    "emailAddr":"james.khoo@secq.me",
    "mobileCountry":"MY",
    "mobileNumber":"162389788",
    "relationship":"parent",
    "networkContact":true
    }
    
     */
    @POST
    @Path("/{userToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewContact(@PathParam("userToken") String userToken, String reqBody) {
        String resultStr = null;
        myLog.debug("Attempt to add new contact for user:->" + userToken + ", reqBody->" + reqBody);
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            JSONObject reqObject = new JSONObject(reqBody);
            ContactVO contactVO = parseContactVO(userVO, reqBody);
            contactManager.addNewContact(userVO, contactVO);
            // To do a refresh of user inorder to all new contact;
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            JSONObject resultObj = new JSONObject();
            resultObj.put(CommonJSONKey.CONTACT_LIST_KEY, parseUserContactListAsJSONArray(userVO));
            populateUserSafetyLevelList(userVO, resultObj);
            resultStr = resultObj.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;
    }
    
    
    /*
     * 
     */
    @GET
    @Path("/{userToken}/sendnotification")
    @Produces(MediaType.APPLICATION_JSON)
    public String sendNotificationToContacts(@PathParam("userToken") String userToken) {
        String resultStr = null;
        myLog.debug("Attempt to sendNotificationToContacts:->" + userToken);

            UserVO userVO = userManager.getUserByActivationCode(userToken);
            userManager.sendNotificationToContacts(userVO);

        return OK_STATUS;
    }
    
    /*
    {      
    "contactIdList":[123,123]
    }
    
     */
    @POST
    @Path("/{userToken}/resend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String resendNotification(@PathParam("userToken") String userToken, String reqBody) {
        myLog.debug("Attempt to resend notification from user:->" + userToken + ", reqBody->" + reqBody);
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            JSONObject reqObject = new JSONObject(reqBody);
            List<Long> contactIdList = new ArrayList<Long>();
            if(reqObject.has(CommonJSONKey.CONTACT_ID_LIST_KEY)){
            	JSONArray jsonArray  = reqObject.getJSONArray(CommonJSONKey.CONTACT_ID_LIST_KEY);
            	for(int i=0;i<jsonArray.length();i++) {
            		Long contactId = jsonArray.getLong(i);
            		contactIdList.add(contactId);
            	}
            	userManager.resendNotificationToContact(userVO, contactIdList);
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return OK_STATUS;
    }

    private ContactVO parseContactVO(UserVO userVO, String contactVOStr) throws JSONException, CoreException {
        JSONObject contactObj = new JSONObject(contactVOStr);
        ContactVO contactVO = new ContactVO();
        contactVO.setNickName(contactObj.getString(CommonJSONKey.USER_NAME_KEY));
        contactVO.setUserVO(userVO);

        if (contactObj.has(ContactVO.CONTACT_ID_KEY)) {
            contactVO.setId(contactObj.getLong(ContactVO.CONTACT_ID_KEY));
        }

        if (contactObj.has(ContactVO.EMAIL_ADDR_KEY)) {
            contactVO.setEmailAddress(contactObj.getString(ContactVO.EMAIL_ADDR_KEY));
        }

        if (contactObj.has(CommonJSONKey.MOBILE_COUNTRY_KEY)) {
            CountryVO countryVO = smsManager.getCountry(contactObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY));
            contactVO.setCountryVO(countryVO);
        }

        if (contactObj.has(CommonJSONKey.MOBILE_NO_KEY)) {
            String mobileNumber = contactObj.getString(CommonJSONKey.MOBILE_NO_KEY);
            
            
            mobileNumber = mobileNumber.replace("-", "").replace("(","").replace(")","").replace(" ", "");
            contactVO.setMobileNo(mobileNumber);
        }

        if (contactObj.has(CommonJSONKey.RELATIONSHIP_KEY)) {
            contactVO.setRelationship(contactObj.getString(CommonJSONKey.RELATIONSHIP_KEY));
        }
        
        //verify and set mobile number without additional characters and leading 0
        if (contactVO.getCountryVO() != null && contactVO.getMobileNo() != null) {
        	boolean validNumber = mobileNumberUtil.isValidMobileNumber(contactVO.getMobileNo(), contactVO.getCountryVO().getIso());
            if (validNumber) {
            	String updatedMobileNumber = mobileNumberUtil.checkAndReturnCorrectMobileNumber(contactVO.getMobileNo(), 
            			Integer.parseInt(contactVO.getCountryVO().getCallingCode()));
            	contactVO.setMobileNo(updatedMobileNumber);
            } else {
            	throw new CoreException(ErrorType.USER_CONTACT_MOBILE_NUMBER_INVALID, userVO.getLangCode(), 
            			"+" + contactVO.getCountryVO().getCallingCode() + contactVO.getMobileNo());
            }
        }

        return contactVO;
    }

}

package com.secqme.rs.v2;

import com.secqme.CoreException;
import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.TrackingLogVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.event.EventType;
import com.secqme.domain.model.event.FullEventInfoVO;
import com.secqme.domain.model.event.SavedEventVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.rs.BaseResource;
import com.secqme.util.MediaFileType;
import com.sun.jersey.api.ConflictException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
  TODO Support multipart upload of
 */
@Path("/v2/event")
public class EventResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(EventResource.class);
    private final static Long MILLISECONDS_IN_A_MINUTE = 60l * 1000;
    private final static Long MILLISECONDS_IN_A_SECOND = 1000l;
    private final static String OK_RESULT_WITH_DURATION = "{\"status\":\"extend\", \"duration\":%1$d}"; //khlow20120607
    private final static String WOM_DEFAULT_MSG_KEY = "wom.event.default.msg";
    private final static String WOM_DEFAULT_EMERGENCY_MSG_KEY = "wom.event.emergency.default.msg";

    public EventResource() {
    }

    @Path("/history/{userAuthToken}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllUserPastEvent(@PathParam("userAuthToken") String authToken) {
        // TODO
        return getUserPastEventWithPagination(authToken, null, null);
    }

    @Path("/history/{userAuthToken}/{startRecord}/{maxRecord}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserPastEventWithPagination(@PathParam("userAuthToken") String authToken,
                                                 @PathParam("startRecord") Integer startRecord,
                                                 @PathParam("maxRecord") Integer maxRecord) {
        JSONObject resultObject = new JSONObject();
        try {
            UserVO userVO = null;
            userVO = userManager.getUserByActivationCode(authToken);
            List<SecqMeEventVO> secqMeEventVOList = eventManager.getUserEventHistory(userVO, startRecord, maxRecord);
            resultObject.put(CommonJSONKey.EVENT_HISTORY_LIST_KEY, prepareUserEventHistoryArray(secqMeEventVOList));
        } catch (JSONException ex) {
            myLog.error("JSONException", ex);
        }
        return resultObject.toString();
    }

    @Path("/end")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String endEvent(String reqBody) {
        boolean userInDanger = false;
        Double latitude = null;
        Double longitude = null;
        Double accuracy = null;
        JSONObject result = new JSONObject();
        /*
        2. New Request
        {
        "authToken":"62b34fae-db8a-482c-b8bf-e05881696872",
        "location":{               // User's location when end
        		"latitude":3.06766,  
                "longitude":101.62, 
        		"accuracy":"100"
        	} 
        }
         */
        try {
        	
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(CommonJSONKey.AUTH_TOKEN_KEY)) {
                String token = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }
            if (jobj.has(CommonJSONKey.LOCATION_KEY)) {
                JSONObject locationObject = jobj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                latitude = locationObject.getDouble(CommonJSONKey.LATITUDE_KEY);
                longitude = locationObject.getDouble(CommonJSONKey.LONGITUDE_KEY);
                accuracy = locationObject.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY);
            }
            

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken or invalid authToken");
            }
            eventManager.confirmSafetyForUserLatestEvent(userVO, true, latitude, longitude, accuracy);
            //to return available sms credit
            result.put("result", "ok");
            BillingCycleVO billCycleVO = billingManager.getUserLatestBillCycleVO(userVO);
            if(billCycleVO != null) {
            	result.put(BillingCycleVO.SMS_CREDIT_BALANCE_KEY,
                        billCycleVO.getSMSCreditBalanceText());
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        if (userInDanger) {
            return NOT_OK_RESULT;
        }
        return result.toString();
    }

    @Path("/{authToken}/{eventId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getFullEventInfo(@PathParam("authToken") String authToken, @PathParam("eventId") Long eventId) {
        String result;

        UserVO userVO = userManager.getUserByActivationCode(authToken);
        FullEventInfoVO fullEventInfoVO = eventManager.getFullEventInfo(userVO, eventId);
        result = fullEventInfoVO.toJSON().toString();

        return result;
    }

    @Path("/{appid}/{appsecret}/{eventId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getFullEventInfoOfContact(@PathParam("appid") String applicationId,
            @PathParam("appsecret") String applicationSecret, @PathParam("eventId") Long eventId) {
    	
        String result;

        sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
        FullEventInfoVO fullEventInfoVO = eventManager.getFullEventInfoOfContact(eventId);
        result = fullEventInfoVO.toJSON().toString();

        return result;
    }
    /*
      {
        "authToken":"THE_TOKEN",
        "eventMsg":"New Message",     // optional
        "pictureStream":"DGAFASDF",   // optional
        "location":{                  // optional
              "latitude":3.1386835070985,
              "longitude":101.610431405406,
              "accuracy":20
        }
      }
     */
    @Path("/{eventId}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateEventLog(@PathParam("eventId") Long eventId, String reqBody) {
        String result;
        String encodedPicStr = null;
        String eventMsg = null;
        Double latitude = null;
        Double longitude = null;
        Double approximate = null;
        try {
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO;
            String token = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
            userVO = userManager.getUserByActivationCode(token);

            if (userVO == null) {
                throw new ConflictException("Invalid AuthToken");
            }
            if (jobj.has(CommonJSONKey.EVENT_MESSAGE_KEY)) {
                eventMsg = jobj.getString(CommonJSONKey.EVENT_MESSAGE_KEY);
            }

            if (jobj.has(CommonJSONKey.PICTURE_STREAM_KEY)) {
                encodedPicStr = jobj.getString(CommonJSONKey.PICTURE_STREAM_KEY);
            }
            if (jobj.has(CommonJSONKey.LOCATION_KEY)) {
                JSONObject locationObject = jobj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                latitude = locationObject.getDouble(CommonJSONKey.LATITUDE_KEY);
                longitude = locationObject.getDouble(CommonJSONKey.LONGITUDE_KEY);
                approximate = locationObject.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY);
            }

            SecqMeEventVO eventVO = eventManager.updateEventLog(eventId, eventMsg, encodedPicStr, MediaFileType.PICTURE,
                    latitude, longitude, approximate);
            result = prepareRegisteredEventJSONObject(eventVO).toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return result;
    }


    /*
       {
        "mobileCountry":"MY",
        "mobileNumber":"162389788",
        "langCode":"en_US"
        }
     */

    @POST
    @Path("/trialevent/notify/{appid}/{appsecret}/{eventId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String notifyTrialEvent(@PathParam("appid") String applicationId,
                                    @PathParam("appsecret") String applicationSecret,
                                    @PathParam("eventId") Long eventID,
                                    String reqBody) {
        String result = "";
        String userMobileNumber;
        String userMobileCountry;
        String langCode;
        try {
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            JSONObject reqObj = new JSONObject(reqBody);
            userMobileCountry = reqObj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY);
            userMobileNumber = reqObj.getString(CommonJSONKey.MOBILE_NO_KEY);
            langCode = reqObj.getString(CommonJSONKey.LANG_CODE_KEY);
            eventManager.notifyTrialEvent(eventID, userMobileCountry, userMobileNumber, langCode);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return OK_STATUS;
    }

    /*
         {
           "duration":20,
           "location":{"latitude":3.06766,"longitude":101.62, "accuracy":"100"}
         }
    */
    @Path("/trialevent/{appid}/{appsecret}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String registerTrialEvent(@PathParam("appid") String applicationId,
                                     @PathParam("appsecret") String applicationSecret,
                                     String reqBody) {
        String result = null;
        Integer eventDuration;
        Double latitude;
        Double longitude;
        Double accuracy;


        try {
            myLog.debug("Register Trial Event->reqBody," + reqBody);
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            JSONObject reqObj = new JSONObject(reqBody);
            eventDuration = reqObj.getInt(CommonJSONKey.DURATION_KEY);
            myLog.debug("Register Trial Event, duration=" + eventDuration);
            SecqMeEventVO registeredEvent = eventManager.registerTryEvent(eventDuration);
            if (registeredEvent != null && reqObj.has(CommonJSONKey.LOCATION_KEY)) {
                JSONObject locationObj = reqObj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
                accuracy = locationObj.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY);
                TrackingLogVO trackingLogVO = new TrackingLogVO();
                trackingLogVO.setSecqMeEventVO(registeredEvent);
                trackingLogVO.setLatitude(latitude);
                trackingLogVO.setLongitude(longitude);
                trackingLogVO.setAccuracy(accuracy);
                trackingManager.submitNewTrackingLog(trackingLogVO);
            }

            JSONObject resultObj = prepareRegisteredEventJSONObject(registeredEvent);
            result = resultObj.toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return result;

    }


    /*
          {
            "authToken":"{{userAuthToken}}",
            "eventType":"NORMAL",
            "message":"Working at Plz Damas",
            "duration":20,
            "location":{"latitude":3.06766,"longitude":101.62, "accuracy":"100"}
          }
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createNewEvent(String reqBody) {
        String result = null;
        String encodedAudioStream = null;
        String encodedPicStream = null;
        String encodedVideoStream = null;
        Boolean triggerNotificaton = null;
        Double latitude = null;
        Double longitude = null;
        Double accuracy = null;

        try {
            JSONObject reqObj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (reqObj.has(CommonJSONKey.AUTH_TOKEN_KEY)) {
                String token = reqObj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken, or passing AuthToken is not valid ");
            }


            // Attemp to build the new SecqMeEvent
            SecqMeEventVO secqMeEvent = new SecqMeEventVO();
            Date startTime = new Date();
            secqMeEvent.setStartTime(startTime);
            secqMeEvent.setEventType(EventType.EMERGENCY);

            secqMeEvent.setEventType(EventType.valueOf(reqObj.getString(CommonJSONKey.EVENT_TYPE_KEY)));

            if (reqObj.has(CommonJSONKey.EVENT_MESSAGE_KEY)) {
                secqMeEvent.setMessage(reqObj.getString(CommonJSONKey.EVENT_MESSAGE_KEY));
            } else {
                String defaultWOMMsg = beanUtil.getMessage(WOM_DEFAULT_MSG_KEY, userVO.getLangCode());
                secqMeEvent.setMessage(defaultWOMMsg);
            }

            if (EventType.EMERGENCY.equals(secqMeEvent.getEventType())
                    && (secqMeEvent.getMessage() == null
                    || StringUtils.isEmpty(secqMeEvent.getMessage()))) {

                secqMeEvent.setMessage(beanUtil.getMessage(WOM_DEFAULT_EMERGENCY_MSG_KEY, userVO.getLangCode()));
            }

            secqMeEvent.setEnableGPS(true);
            //secqMeEvent.setEnableSafetyNotify(Boolean.TRUE);


            if (reqObj.has(CommonJSONKey.AUDIO_STREAM_KEY)) {
                encodedAudioStream = reqObj.getString(CommonJSONKey.AUDIO_STREAM_KEY);
            }

            if (reqObj.has(CommonJSONKey.PICTURE_STREAM_KEY)) {
                encodedPicStream = reqObj.getString(CommonJSONKey.PICTURE_STREAM_KEY);
            }

            if (reqObj.has(CommonJSONKey.VIDEO_STREAM_KEY)) {
                encodedVideoStream = reqObj.getString(CommonJSONKey.VIDEO_STREAM_KEY);
            }

            if (reqObj.has(CommonJSONKey.TRIGGER_NOTIFICATION_KEY)) {
                triggerNotificaton = reqObj.getBoolean(CommonJSONKey.TRIGGER_NOTIFICATION_KEY);
            }


            if (EventType.NORMAL.equals(secqMeEvent.getEventType())) {
                Date endTime = null;
                if (reqObj.has(CommonJSONKey.DURATION_KEY)) {
                    endTime = new Date(startTime.getTime() + (reqObj.getLong(CommonJSONKey.DURATION_KEY) * MILLISECONDS_IN_A_MINUTE));
                } else if (reqObj.has(CommonJSONKey.DURATION_IN_SECOND_KEY)) {
                    endTime = new Date(startTime.getTime() + (reqObj.getLong(CommonJSONKey.DURATION_IN_SECOND_KEY) * MILLISECONDS_IN_A_SECOND));
                }

                if (endTime == null) {
                    throw new ConflictException("New Event Registration require->" +
                            CommonJSONKey.DURATION_KEY + ", or " + CommonJSONKey.DURATION_IN_SECOND_KEY + ", parameters");
                }
                secqMeEvent.setEndTime(endTime);
            }
            secqMeEvent.setUserVO(userVO);

            myLog.debug("Register Event, user->" + secqMeEvent.getUserVO().getUserid() +
                    ", eventType->" + secqMeEvent.getEventType() +
                    ", audioEnable->" + (encodedAudioStream == null) +
                    ", videoEnable->" + (encodedVideoStream == null) +
                    ", triggerNotification->" + triggerNotificaton +
                    ", eventMessage->" + secqMeEvent.getMessage());

            SecqMeEventVO registeredEvent = eventManager.registerNewEvent(secqMeEvent,
                    encodedPicStream,
                    encodedAudioStream,
                    encodedVideoStream, triggerNotificaton);

            if (registeredEvent != null && reqObj.has(CommonJSONKey.LOCATION_KEY)) {
                JSONObject locationObj = reqObj.getJSONObject(CommonJSONKey.LOCATION_KEY);
                latitude = locationObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                longitude = locationObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
                accuracy = locationObj.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY);
                TrackingLogVO trackingLogVO = new TrackingLogVO();
                trackingLogVO.setSecqMeEventVO(registeredEvent);
                trackingLogVO.setLatitude(latitude);
                trackingLogVO.setLongitude(longitude);
                trackingLogVO.setAccuracy(accuracy);

                trackingManager.submitNewTrackingLog(trackingLogVO);
            }
            JSONObject resultObj = prepareRegisteredEventJSONObject(registeredEvent);
            populateUserSafetyLevelList(userVO, resultObj);
            result = resultObj.toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return result;
    }

    @Path("/emergency/{authToken}/{eventId}")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitEmergencyVideoWithEventID(
            @PathParam("authToken") String authToken,
            @PathParam("eventId") Long eventId,
            @FormDataParam("file") InputStream videoInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("notify") String notify) {

        String resultStr = null;
        UserVO userVO = null;
        boolean toNotify = false;

        try {
            userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Submitting emergency Video for user->" + userVO.getUserid() + " To notify->" + notify);

            if ("false".equalsIgnoreCase(notify)) {
                toNotify = false;
            }
            SecqMeEventVO eventVO = new SecqMeEventVO();
            eventVO.setUserVO(userVO);
            eventVO.setStartTime(new Date());
            eventVO.setMessage(EventType.EMERGENCY.name());
            eventVO.setEventType(EventType.EMERGENCY);
            SecqMeEventVO registeredEventVO =
                    eventManager.mergeEmergencyVideoIntoCurrentEvent(eventId, eventVO, null, videoInputStream, toNotify);

            resultStr = prepareRegisteredEventJSONObject(registeredEventVO).toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;

    }

    //khlow20120605
    // TODO Check,
    @Path("/extend")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String extendCurrentEvent(String reqBody) {
        boolean eventFound = false;
        long duration = 0;
        /*
        2. New Request
        { 
        "authToken":"7f096930-7d48-4bbd-931c-f6d0165bd2e4", 
        "duration":"10"
        }
         */
        try {

            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(CommonJSONKey.AUTH_TOKEN_KEY)) {
                String token = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }

            if (!jobj.has(CommonJSONKey.DURATION_KEY)) {
                throw new ConflictException("Missing require duration parameter");
            }

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken or invalid authToken");
            }
            duration = jobj.getLong(CommonJSONKey.DURATION_KEY);
            eventFound = eventManager.extendCurrentEvent(userVO, (duration));
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        if (eventFound) {
            return String.format(OK_RESULT_WITH_DURATION, duration);
        }
        return NOT_OK_RESULT;
    }

    @Path("/updateSNS")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateSNSManually(String reqBody) {
        boolean postSuccess = false;
        SecqMeEventVO secqMeEventVO = null;
        /*
        2. New Request
        { 
        "authToken":"7f096930-7d48-4bbd-931c-f6d0165bd2e4"
        }
         */
        try {

            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(CommonJSONKey.AUTH_TOKEN_KEY)) {
                String token = jobj.getString(CommonJSONKey.AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken or invalid authToken");
            } else {
                secqMeEventVO = eventManager.getUserLatestEvent(userVO.getUserid());
            }
            postSuccess = eventManager.updateSNSManually(secqMeEventVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        if (postSuccess) {
            return OK_STATUS;
        }
        return NOT_OK_RESULT;
    }

    /*Example 1 -- 
    {      
    "id":"1",
    "eventName":"walk to car",
    "userid":"qwe@123.com",
    "eventDuration":5,
    "defaultEvent":0,
    "optionalDescription":"something"
    }
    */
    @PUT
    @Path("/updateSavedEvent/{usertoken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateSaveEvent(@PathParam("usertoken") String userToken,
                                  String reqBody) {
        myLog.debug("Attempt to update SaveEvent->" + reqBody);
        String resultStr = null;
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            SavedEventVO savedEventVO = parseSavedEventsVO(userVO, reqBody);
            myLog.debug("replacing event for user:" + userVO.getUserid() + ", eventid:" + savedEventVO.getId());
            quickEventManager.updateUserSavedEvent(userVO,
                    savedEventVO.getId(), savedEventVO.getEventName(), savedEventVO.getEventDuration(),
                    savedEventVO.getOptionalDescription());
            userVO = userManager.getUserInfoByUserId(userVO.getUserid());
            resultObject.put(CommonJSONKey.SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }

    @POST
    @Path("/deleteSavedEvent/{usertoken}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteSaveEvent(@PathParam("usertoken") String userToken,
                                  String reqBody) {
        String resultStr = null;
        myLog.debug("Attemp to delete event for user:->" + userToken + ", reqBody->" + reqBody);
        try {
            JSONObject resultObject = new JSONObject();
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            SavedEventVO savedEventVO = parseSavedEventsVO(userVO, reqBody);
            quickEventManager.deleteUserSavedEvent(userVO, savedEventVO.getId());
            resultObject.put(CommonJSONKey.SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;
    }

    @POST
    @Path("/addSavedEvent/{usertoken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewSaveEvent(@PathParam("usertoken") String userToken, String reqBody) {
        String resultStr = null;
        myLog.debug("Attemp to add new saved event for user:->" + userToken + ", reqBody->" + reqBody);
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            SavedEventVO savedEventVO = parseSavedEventsVO(userVO, reqBody);
            quickEventManager.createNewSavedEvent(userVO, savedEventVO.getEventName(), savedEventVO.getEventDuration(),
                    savedEventVO.getOptionalDescription());
            JSONObject resultObject = new JSONObject();
            resultObject.put(CommonJSONKey.SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
            resultStr = resultObject.toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;
    }

    @POST
    @Path("/savedEventList/{usertoken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getSavedEventList(@PathParam("usertoken") String userToken) {
        String resultStr = null;
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            JSONObject resultObject = new JSONObject();
            resultObject.put(CommonJSONKey.SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
            resultStr = resultObject.toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;
    }

    /* share event
    { 
    "authToken":"7f096930-7d48-4bbd-931c-f6d0165bd2e4",
    "eventId":12345,
    "contactList":[1001,1002] //optional
    }
     */
    @POST
    @Path("/shareevent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String shareEvent(String reqBody) {
    	JSONObject result = new JSONObject();
        try {
            JSONObject requestObj = new JSONObject(reqBody);
            UserVO userVO = userManager.getUserByActivationCode(requestObj.getString(CommonJSONKey.AUTH_TOKEN_KEY));
            Long eventId = requestObj.getLong(CommonJSONKey.EVENT_ID_KEY);
            if(requestObj.has(CommonJSONKey.CONTACT_LIST_KEY)) {
            	JSONArray requestArray = requestObj.getJSONArray(CommonJSONKey.CONTACT_LIST_KEY);
            	List<Long> contactList = new ArrayList<Long>();
            	myLog.debug("ShareEvent " + requestArray.toString());
            	for(int i=0;i<requestArray.length();i++) {
            		contactList.add(requestArray.getLong(i));
            	}
            	eventManager.shareEvent(userVO, eventId, contactList);
            } else {
            	eventManager.shareEvent(userVO, eventId, null);
            }
            
            //to return available sms credit
            result.put("result", "ok");
            BillingCycleVO billCycleVO = billingManager.getUserLatestBillCycleVO(userVO);
            if(billCycleVO != null) {
            	result.put(BillingCycleVO.SMS_CREDIT_BALANCE_KEY,
                        billCycleVO.getSMSCreditBalanceText());
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return result.toString();
    }
    
    private JSONArray getUserEventListAsJSONArray(UserVO userVO) throws JSONException, CoreException {
        JSONArray eventArray = new JSONArray();
        List<SavedEventVO> userEventList = quickEventManager.getUserSavedEventList(userVO);
        if (userEventList != null && userEventList.size() > 0) {
            //Collections.sort(userEventList);
            for (SavedEventVO savedEventVO : userEventList) {
                eventArray.put(savedEventVO.getJSONObject());
            }
        }
        return eventArray;
    }

    private SavedEventVO parseSavedEventsVO(UserVO userVO, String eventVOStr) throws JSONException {
        JSONObject eventObj = new JSONObject(eventVOStr);
        SavedEventVO savedEventVO = new SavedEventVO();
        savedEventVO.setUserVO(userVO);

        if (eventObj.has(SavedEventVO.EVENT_ID_KEY)) {
            savedEventVO.setId(eventObj.getLong(SavedEventVO.EVENT_ID_KEY));
        }

        if (eventObj.has(SavedEventVO.EVENT_NAME_KEY)) {
            savedEventVO.setEventName(eventObj.getString(SavedEventVO.EVENT_NAME_KEY));
        }

        if (eventObj.has(SavedEventVO.EVENT_DURATION_KEY)) {
            savedEventVO.setEventDuration(eventObj.getInt(SavedEventVO.EVENT_DURATION_KEY));
        }

        if (eventObj.has(SavedEventVO.EVENT_DEFAULT_KEY)) {
            savedEventVO.setDefaultEvent(eventObj.getInt(SavedEventVO.EVENT_DEFAULT_KEY));
        }

        if (eventObj.has(SavedEventVO.EVENT_DESCRIPTION_KEY)) {
            savedEventVO.setOptionalDescription(eventObj.getString(SavedEventVO.EVENT_DESCRIPTION_KEY));
        }
        return savedEventVO;
    }

    private JSONArray prepareUserEventHistoryArray(List<SecqMeEventVO> secqMeEventVOList) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        if (secqMeEventVOList != null && secqMeEventVOList.size() > 0) {
            JSONObject eventObj;
            for (SecqMeEventVO secqMeEventVO : secqMeEventVOList) {
                eventObj = new JSONObject();
                eventObj.put(SecqMeEventVO.EVENT_ID_KEY, secqMeEventVO.getId())
                        .put(CommonJSONKey.EVENT_TYPE_KEY, secqMeEventVO.getEventType())
                        .put(CommonJSONKey.EVENT_START_TIME_KEY, secqMeEventVO.getStartTime().getTime())
                        .put(CommonJSONKey.EVENT_MESSAGE_KEY, secqMeEventVO.getMessage())
                        .put(CommonJSONKey.TIME_ZONE_KEY, secqMeEventVO.getEventTimeZone());

                if (secqMeEventVO.getTrackingURL() != null) {
                    eventObj.put(CommonJSONKey.EVENT_TRACKING_URL, secqMeEventVO.getTrackingURL());
                }

                if (EventStatusType.END == secqMeEventVO.getStatus()
                        || EventStatusType.SAFE_NOTIFY == secqMeEventVO.getStatus()) {
                    eventObj.put(CommonJSONKey.CONFIRM_SAFETY, Boolean.TRUE);
                    if (secqMeEventVO.getConfirmSafetyTime() != null) {
                        eventObj.put(CommonJSONKey.CONFIRM_SAFETY_TIME, secqMeEventVO.getConfirmSafetyTime().getTime());
                    }
                }


                if (secqMeEventVO.getEventDurationInMinutes() != null) {
                    eventObj.put(CommonJSONKey.EVENT_DURATION_KEY, secqMeEventVO.getEventDurationInMinutes());
                }

                if (secqMeEventVO.getEndTime() != null) {
                    eventObj.put(CommonJSONKey.EVENT_END_TIME_KEY, secqMeEventVO.getEndTime().getTime());
                }
                jsonArray.put(eventObj);
            }
        }
        return jsonArray;
    }

    private JSONObject prepareRegisteredEventJSONObject(SecqMeEventVO registeredEvent) throws JSONException, CoreException {
        JSONObject resultObj = new JSONObject();
        resultObj.put(CommonJSONKey.EVENT_TYPE_KEY, registeredEvent.getEventType())
                .put(SecqMeEventVO.EVENT_ID_KEY, registeredEvent.getId())
                .put(CommonJSONKey.EVENT_STATUS_KEY, registeredEvent.getStatus())
                .put(CommonJSONKey.EVENT_MESSAGE_KEY, registeredEvent.getMessage())
                .put(CommonJSONKey.USER_CURRENT_BALANCE_KEY, registeredEvent.getUserVO()
                        .getCurrentBalance()).put(CommonJSONKey.TRACKING_PIN_KEY, registeredEvent.getTrackingPin());

        if (registeredEvent.getTrackingURL() != null) {
            resultObj.put(CommonJSONKey.EVENT_TRACKING_URL, registeredEvent.getTrackingURL());
        }

        resultObj.put(CommonJSONKey.TOTAL_EVENT_REGISTERED_KEY,
                eventManager.getUserTotalRegisteredEvent(registeredEvent.getUserVO().getUserid()));

        // JK May 19 2011,
        // Attempt to find out the user Balance, and SMS Credit
        //
        BillingCycleVO billCycleVO = billingManager.getUserLatestBillCycleVO(registeredEvent.getUserVO());

        // TODO Event Time, + Event Duration
        if (billCycleVO != null) {
            resultObj.put(BillingCycleVO.EVENT_CREDIT_BALANCE_KEY,
                    billCycleVO.getEventCreditBalanceText());
            resultObj.put(BillingCycleVO.SMS_CREDIT_BALANCE_KEY,
                    billCycleVO.getSMSCreditBalanceText());
        }

        if (registeredEvent.getEventDurationInMinutes() != null) {
            resultObj.put(CommonJSONKey.EVENT_DURATION_KEY, registeredEvent.getEventDurationInMinutes());
        }

        if (registeredEvent.getEventDurationInSeconds() != null) {
            resultObj.put(CommonJSONKey.DURATION_IN_SECOND_KEY, registeredEvent.getEventDurationInSeconds());
        }

        if (registeredEvent.getStartTime() != null) {
            resultObj.put(CommonJSONKey.EVENT_START_TIME_KEY, registeredEvent.getStartTime().getTime());
        }

        return resultObj;
    }
}

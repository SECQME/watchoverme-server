package com.secqme.rs;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.event.EventType;
import com.secqme.domain.model.event.FullEventInfoVO;
import com.secqme.domain.model.event.SavedEventVO;
import com.secqme.domain.model.event.SecqMeEventVO;
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
import java.util.Date;
import java.util.List;

/*
  TODO Support multipart upload of
 */
@Path("/event")
public class EventResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(EventResource.class);
    private final static Long MILLISECONDS_IN_A_MINUTE = 60l * 1000;
    private final static Long MILLISECONDS_IN_A_SECOND = 1000l;
    private final static String AUTH_TOKEN_KEY = "authToken";
    private final static String EVENT_PIN_KEY = "eventPin";
    private final static String EVENT_MESSAGE_KEY = "message";
    private final static String ENABLE_GPS_KEY = "enableGPS";
    private final static String ENABLE_SAFETY_KEY = "enableSafetyNotify";
    private final static String AUDIO_STREAM_KEY = "audioStream";
    private final static String PICTURE_STREAM_KEY = "pictureStream";
    private final static String VIDEO_STREAM_KEY = "videoStream";
    private final static String DURATION_KEY = "duration"; // durationInMinute
    private final static String DURATION_IN_SECOND_KEY = "durationInSecond"; // durationInSecond
    private final static String EVENT_TYPE_KEY = "eventType";
    private final static String EVENT_STATUS_KEY = "eventStatus";
    private final static String USER_CURRENT_BALANCE_KEY = "userCurrentBalance";
    private final static String TRACKING_PIN_KEY = "trackingPin";
    private final static String EVENT_DURATION_KEY = "eventDuration";
    private final static String EVENT_START_TIME_KEY = "eventStartTime";
    private final static String NEW_EVENT_START_TIME_KEY = "newEventStartTime";
    private final static String TRIGGER_NOTIFICATION_KEY = "triggerNotification";
    private final static String SAVED_EVENTS_KEY = "savedEvents";
    private final static String NOTIFY_SMS_KEY = "sms";
    private final static String NOTIFY_EMAIL_KEY = "email";
    private final static String NOTIFY_FACEBOOK_KEY = "facebook";
    private final static String OK_RESULT_WITH_DURATION = "{\"status\":\"extend\", \"duration\":%1$d}"; //khlow20120607
    private final static String WOM_DEFAULT_MSG_KEY = "wom.event.default.msg";
    private final static String WOM_DEFAULT_EMERGENCY_MSG_KEY = "wom.event.emergency.default.msg";
    private final static String LOCATION_KEY = "location";
    private final static String LATITUDE_KEY = "latitude";
    private final static String LONGITUDE_KEY = "longitude";
    private final static String APPROXIMATE_KEY = "approx";

    public EventResource() {
    }

    @Path("/history")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserPastEvent() {
        return "";
    }

    @Path("/end")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String endEvent(String reqBody) {
        boolean userInDanger = false;
        /*
        2. New Request
        { 
        "authToken":"62b34fae-db8a-482c-b8bf-e05881696872", 
        }
         */
        try {

            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(AUTH_TOKEN_KEY)) {
                String token = jobj.getString(AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken or invalid authToken");
            }
            eventManager.confirmSafetyForUserLatestEvent(userVO, true);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        if (userInDanger) {
            return NOT_OK_RESULT;
        }
        return OK_STATUS;
    }

    @Path("/{authToken}/{eventId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getFullEventInfo(@PathParam("authToken") String authToken, @PathParam("eventId") Long eventId)  {
        String result;

        UserVO userVO = userManager.getUserByActivationCode(authToken);
        if (userVO == null) {
            throw new ConflictException("Invalid AuthToken");
        }
        FullEventInfoVO fullEventInfoVO = eventManager.getFullEventInfo(userVO, eventId);
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
              "approx":20
        }
      }
     */
    @Path("/{eventId}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String updateEventLog(@PathParam("eventId") Long eventId, String reqBody) {
        String result = null;
        String encodedPicStr = null;
        String eventMsg = null;
        Double latitude = null;
        Double longitude = null;
        Double approximate = null;
        try {
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            String token = jobj.getString(AUTH_TOKEN_KEY);
            userVO = userManager.getUserByActivationCode(token);

            if (userVO == null) {
                throw new ConflictException("Invalid AuthToken");
            }
            if (jobj.has(EVENT_MESSAGE_KEY)) {
                eventMsg = jobj.getString(EVENT_MESSAGE_KEY);
            }

            if (jobj.has(PICTURE_STREAM_KEY)) {
                encodedPicStr = jobj.getString(PICTURE_STREAM_KEY);
            }
            if (jobj.has(LOCATION_KEY)) {
                JSONObject locationObject = jobj.getJSONObject(LOCATION_KEY);
                latitude = locationObject.getDouble(LATITUDE_KEY);
                longitude = locationObject.getDouble(LONGITUDE_KEY);
                approximate = locationObject.getDouble(APPROXIMATE_KEY);
            }

            SecqMeEventVO eventVO = eventManager.updateEventLog(eventId, eventMsg, encodedPicStr, MediaFileType.PICTURE,
                    latitude, longitude, approximate);
            result = prepareRegisteredEventJSONObject(eventVO).toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return result;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createNewEvent(String reqBody) {
        String result = null;
        String encodedAudioStream = null;
        String encodedPicStream = null;
        String encodedVideoStream = null;
        Boolean triggerNotificaton = null;


        /*
         *  New Request
        {
          "authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084" ,
          "eventType":"NORMAL",
          "eventType":"EMERGENCY"
          "message":"Working at Plz Damas",
          "duration":30,
          "pictureStream":"DGAFASDF",
          "durationInSecond":30,
          "videoStream":"23432"
          "audioStream":"DFSDFSDFSDFSDF",
          "triggerNotification":false,
        }
        */


        try {
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(AUTH_TOKEN_KEY)) {
                String token = jobj.getString(AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken, or passin AuthToken is not valid ");
            }


            // Attemp to build the new SecqMeEvent
            SecqMeEventVO secqMeEvent = new SecqMeEventVO();
            Date startTime = new Date();
            secqMeEvent.setStartTime(startTime);
            secqMeEvent.setEventType(EventType.EMERGENCY);

            secqMeEvent.setEventType(EventType.valueOf(jobj.getString(EVENT_TYPE_KEY)));

            if (jobj.has(EVENT_MESSAGE_KEY)) {
                secqMeEvent.setMessage(jobj.getString(EVENT_MESSAGE_KEY));
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

            if (jobj.has(ENABLE_SAFETY_KEY)) {
                secqMeEvent.setEnableSafetyNotify(jobj.getBoolean(ENABLE_SAFETY_KEY));
            }

            if (jobj.has(AUDIO_STREAM_KEY)) {
                encodedAudioStream = jobj.getString(AUDIO_STREAM_KEY);
            }

            if (jobj.has(PICTURE_STREAM_KEY)) {
                encodedPicStream = jobj.getString(PICTURE_STREAM_KEY);
            }

            if (jobj.has(VIDEO_STREAM_KEY)) {
                encodedVideoStream = jobj.getString(VIDEO_STREAM_KEY);
            }

            if (jobj.has(TRIGGER_NOTIFICATION_KEY)) {
                triggerNotificaton = jobj.getBoolean(TRIGGER_NOTIFICATION_KEY);
            }


            if (EventType.NORMAL.equals(secqMeEvent.getEventType())) {
                Date endTime = null;
                if (jobj.has(DURATION_KEY)) {
                    endTime = new Date(startTime.getTime() + (jobj.getLong(DURATION_KEY) * MILLISECONDS_IN_A_MINUTE));
                } else if (jobj.has(DURATION_IN_SECOND_KEY)) {
                    endTime = new Date(startTime.getTime() + (jobj.getLong(DURATION_IN_SECOND_KEY) * MILLISECONDS_IN_A_SECOND));
                }

                if (endTime == null) {
                    throw new ConflictException("New Event Registration require->" +
                            DURATION_KEY + ", or " + DURATION_IN_SECOND_KEY + ", parameters");
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


            result = prepareRegisteredEventJSONObject(registeredEvent).toString();

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
        boolean toNotify = true;

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
            if (jobj.has(AUTH_TOKEN_KEY)) {
                String token = jobj.getString(AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }

            if (!jobj.has(DURATION_KEY)) {
                throw new ConflictException("Missing require duration parameter");
            }

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken or invalid authToken");
            }
            duration = jobj.getLong(DURATION_KEY);
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
            if (jobj.has(AUTH_TOKEN_KEY)) {
                String token = jobj.getString(AUTH_TOKEN_KEY);
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
            resultObject.put(SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
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
            resultObject.put(SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
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
            resultObject.put(SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
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
            resultObject.put(SAVED_EVENTS_KEY, getUserEventListAsJSONArray(userVO));
            resultStr = resultObject.toString();

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;
    }

    @Path("/notify")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String notifySafelyArrival(String reqBody) {
        /*
        2. New Request
        
        { 
        "authToken":"62b34fae-db8a-482c-b8bf-e05881696872", 
        "sms":1,
        "email":1;,
        "facebook":1,
        "twitter":1  -- as for now, we don't support twitter
        }
         */


        boolean toSendSMS = false;
        boolean toSendEmail = false;
        boolean toSendFacebook = false;
        try {
            int sms = 0, email = 0, facebook = 0;
            JSONObject jobj = new JSONObject(reqBody);
            UserVO userVO = null;
            if (jobj.has(AUTH_TOKEN_KEY)) {
                String token = jobj.getString(AUTH_TOKEN_KEY);
                userVO = userManager.getUserByActivationCode(token);
            }
            if (jobj.has(NOTIFY_SMS_KEY)) {
                sms = jobj.getInt(NOTIFY_SMS_KEY);
            }
            if (jobj.has(NOTIFY_EMAIL_KEY)) {
                email = jobj.getInt(NOTIFY_EMAIL_KEY);
            }
            if (jobj.has(NOTIFY_FACEBOOK_KEY)) {
                facebook = jobj.getInt(NOTIFY_FACEBOOK_KEY);
            }

            if (userVO == null) {
                throw new ConflictException("Missing require Parameter of authToken or invalid authToken");
            }

            toSendSMS = (sms == 1);
            toSendEmail = (email == 1);
            toSendFacebook = (facebook == 1);
            eventManager.notifyContactsOnSafelyArrival(userVO, toSendSMS, toSendEmail, toSendFacebook);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return OK_STATUS;
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

    private JSONObject prepareRegisteredEventJSONObject(SecqMeEventVO registeredEvent) throws JSONException {
        JSONObject resultObj = new JSONObject();
        resultObj.put(EVENT_TYPE_KEY, registeredEvent.getEventType())
                .put(SecqMeEventVO.EVENT_ID_KEY, registeredEvent.getId())
                .put(EVENT_STATUS_KEY, registeredEvent.getStatus())
                .put(EVENT_MESSAGE_KEY, registeredEvent.getMessage())
                .put(USER_CURRENT_BALANCE_KEY, registeredEvent.getUserVO()
                        .getCurrentBalance()).put(TRACKING_PIN_KEY, registeredEvent.getTrackingPin());

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
            resultObj.put(EVENT_DURATION_KEY, registeredEvent.getEventDurationInMinutes());
        }

        if (registeredEvent.getEventDurationInSeconds() != null) {
            resultObj.put(DURATION_IN_SECOND_KEY, registeredEvent.getEventDurationInSeconds());
        }

        if (registeredEvent.getStartTime() != null) {
            resultObj.put(EVENT_START_TIME_KEY, registeredEvent.getStartTime().getTime());
            resultObj.put(NEW_EVENT_START_TIME_KEY, (Long) registeredEvent.getStartTime().getTime() / 1000);
        }

        return resultObj;
    }
}

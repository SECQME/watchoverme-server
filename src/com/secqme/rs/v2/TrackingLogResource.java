package com.secqme.rs.v2;

import com.secqme.domain.model.TrackingLogVO;
import com.secqme.domain.model.UserTrackingLogVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/v2/tracking")
public class TrackingLogResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(TrackingLogResource.class);

    public TrackingLogResource() {
    }


    /*
         {
             "latitude":3.1386835070985,
             "longitude":101.610431405406,
             "accuracy":100
         }
    */
    @Path("/trialevent/{appid}/{appsecret}/{eventId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewLocationForTrialEvent(@PathParam("appid") String applicationId,
                                              @PathParam("appsecret") String applicationSecret,
                                              @PathParam("eventId") Long eventId,
                                              String reqBody) {

        try {
            myLog.debug("Adding new Location for Trial Event, eventID->" + eventId + " reqBody->" + reqBody);
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            JSONObject reqObj = new JSONObject(reqBody);

            SecqMeEventVO eventVO = eventManager.findEventByEventID(eventId);
            TrackingLogVO trackingLogVO = new TrackingLogVO();
            trackingLogVO.setSecqMeEventVO(eventVO);
            trackingLogVO.setLatitude(reqObj.getDouble(CommonJSONKey.LATITUDE_KEY));
            trackingLogVO.setLongitude(reqObj.getDouble(CommonJSONKey.LONGITUDE_KEY));
            trackingLogVO.setAccuracy(reqObj.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY));

            trackingManager.submitNewTrackingLog(trackingLogVO);


        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return OK_STATUS;

    }



    /*
     {
       "authToken":"b1d08a7d-f7b4-4944-984f-55930af23d0f",
       "latitude":3.1386835070985,
       "longitude":101.610431405406,
       "accuracy":100
     }
      */
    @POST
    @Path("/log")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewLocationByAuthCode(String reqBody) {

        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString("authToken");
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Attempt to add tracking Log for user->" + userVO.getUserid() + ", reqBody->" +  reqBody);
            SecqMeEventVO latestEvent = eventManager.getUserLatestEvent(userVO.getUserid());
            if(latestEvent != null) {
                myLog.debug("Tracking Log EventID->" + latestEvent.getId());
                TrackingLogVO trackingLogVO = new TrackingLogVO();
                trackingLogVO.setSecqMeEventVO(latestEvent);
                trackingLogVO.setLatitude(jobj.getDouble(CommonJSONKey.LATITUDE_KEY));
                trackingLogVO.setLongitude(jobj.getDouble(CommonJSONKey.LONGITUDE_KEY));
                trackingLogVO.setAccuracy(jobj.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY));

                trackingManager.submitNewTrackingLog(trackingLogVO);
            } else {
            	myLog.debug("no event found for user-> " + userVO.getUserid());
            }

        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return OK_STATUS;
    }

    @POST
    @Path("/usermovement")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewMovementByAuthCode(String reqBody) {

        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString("authToken");
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Attempt to add movement Log for user->" + userVO.getUserid() + ", reqBody->" +  reqBody);
            UserTrackingLogVO userTrackingLogVO = new UserTrackingLogVO();
            userTrackingLogVO.setUserid(userVO.getUserid());
            userTrackingLogVO.setLatitude(jobj.getDouble(CommonJSONKey.LATITUDE_KEY));
            userTrackingLogVO.setLongitude(jobj.getDouble(CommonJSONKey.LONGITUDE_KEY));
            userTrackingLogVO.setAccuracy(jobj.getDouble(CommonJSONKey.LOCATION_ACCURACY_KEY));
            if(jobj.has(CommonJSONKey.BACKGROUND_KEY)) {
                boolean background = false;
                background = jobj.getBoolean(CommonJSONKey.BACKGROUND_KEY);
            	userTrackingLogVO.setBackground(background);
            }
            trackingManager.submitUserMovementLog(userTrackingLogVO);

            userManager.updateLocation(userVO, jobj.getDouble(CommonJSONKey.LATITUDE_KEY), jobj.getDouble(CommonJSONKey.LONGITUDE_KEY), false);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return OK_STATUS;
    }
}

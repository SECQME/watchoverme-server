package com.secqme.rs;

import com.secqme.CoreException;
import com.secqme.domain.model.TrackingLogVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.sun.jersey.api.ConflictException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/tracking")
public class TrackingLogResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(TrackingLogResource.class);

    public TrackingLogResource() {
        super();
    }



    @POST
    @Path("/log")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addNewLocationByAuthCode(String reqBody) {

        //{"authToken":"0416f2af-2a58-401b-8a29-f19c90f2f084", "latitude":3.1386835070985,"longitude":101.610431405406, "approx":100}

        try {
            JSONObject jobj = new JSONObject(reqBody);
            String authToken = jobj.getString("authToken");
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            SecqMeEventVO latestEvent = eventManager.getUserLatestEvent(userVO.getUserid());
            myLog.debug("Attempt to add tracking Log for user->" + userVO.getUserid()  +
                    ", reqBody->" +  reqBody);
            if(latestEvent != null) {
                myLog.debug("The Log Event ID is->" + latestEvent.getId());
            }
            TrackingLogVO trackingLogVO = new TrackingLogVO();
            trackingLogVO.setSecqMeEventVO(latestEvent);
            trackingLogVO.setLatitude(jobj.getDouble("latitude"));
            trackingLogVO.setLongitude(jobj.getDouble("longtitude"));
            trackingLogVO.setAccuracy(jobj.getDouble("accuracy"));
            trackingManager.submitNewTrackingLog(trackingLogVO);
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return OK_STATUS;
    }

}

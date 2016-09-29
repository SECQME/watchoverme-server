package com.secqme.rs.v2;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.crime.CrimeType;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * User: James Khoo
 * Date: 2/10/14
 * Time: 11:43 AM
 */
@Path("/v2/crimereport")
public class CrimeReportResource extends BaseResource {
    private final static Logger myLog = Logger.getLogger(CrimeReportResource.class);

    public CrimeReportResource() {
        // Empty Constructor
    }

    @GET
    @Path("/{latitude}/{longitude}/{distanceInKM}/{historyDays}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findCrimeReportNearMe(@PathParam("latitude") Double latitude,
                                        @PathParam("longitude") Double longitude,
                                        @PathParam("distanceInKM") Integer distanceInKM,
                                        @PathParam("historyDays") Integer historyDays) {
        String resultStr = null;
        JSONObject resultObj = new JSONObject();
        if (distanceInKM == 0) {
            distanceInKM = null;
        }
        if (historyDays == 0) {
            historyDays = null;
        }

        try {
            List<CrimeReportVO> reportVOList = crimeManager.findCrimeReportNearMe(latitude, longitude, distanceInKM, historyDays);
            if (reportVOList != null && reportVOList.size() > 0) {
                resultObj.put(CommonJSONKey.CRIME_REPORT_LIST_KEY, prepareCrimeReportArray(reportVOList));
                resultStr = resultObj.toString();
            } else {
                resultStr = resultObj.put(CommonJSONKey.CRIME_REPORT_LIST_KEY, new JSONArray()).toString();
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }
        return resultStr;
    }

    /*
       {
         "crimeType":"ASSAULT",  // Required, ROBBERY, SHOOTING, VIOLENCE, SEXCRIME, and OTHER
         "note":"Somethings bad happen to me!!"  // Optional

         "crimePic":"ASDASDDSFS",  //Optional Base64 encoded Crime Pic, best to scale the Pic to max width of 320
         "eventId":23423,   // Either report Crime with   Event, or
                            // User is submitting new Report without any Event
                            // Then the following field is require

         "timeRange":"7am-12pm",
         "latitude":3.09,
         "longitude":102.28,
         "accuracy":50
       }
     */
    @POST
    @Path("/{userAuthToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String submitNewCrimeReport(@PathParam("userAuthToken") String authToken, String reqBody) {
        String resultStr = null;
        CrimeReportVO crimeReportVO = null;

        try {
            UserVO userVO = userManager.getUserByActivationCode(authToken);
            myLog.debug("Attempt to add new CrimeReport for user:" + userVO.getUserid() + " reqBody->" + reqBody);
            JSONObject reqObj = new JSONObject(reqBody);

            String crimeNote = reqObj.has(CommonJSONKey.CRIME_NOTE_KEY) ? reqObj.getString(CommonJSONKey.CRIME_NOTE_KEY) : null;
            String crimeTypeStr = reqObj.getString(CommonJSONKey.CRIME_TYPE_KEY);
            String crimePicStr = reqObj.has(CommonJSONKey.CRIME_PICTURE_KEY) ? reqObj.getString(CommonJSONKey.CRIME_PICTURE_KEY) : null;
            CrimeType crimeType = CrimeType.valueOf(crimeTypeStr);
            if (reqObj.has(CommonJSONKey.EVENT_ID_KEY)) {
                Long eventId = reqObj.getLong(CommonJSONKey.EVENT_ID_KEY);
                crimeReportVO = crimeManager.reportNewCrimeWithEvent(userVO, eventId, crimeType,
                        crimeNote, crimePicStr);
            } else {
                String timeRange = reqObj.getString(CommonJSONKey.CRIME_TIME_RANGE_KEY);
                Double latitude = reqObj.getDouble(CommonJSONKey.LATITUDE_KEY);
                Double longitude = reqObj.getDouble(CommonJSONKey.LONGITUDE_KEY);
                crimeReportVO = crimeManager.reportNewCrime(userVO, crimeType, timeRange, crimeNote, crimePicStr,
                        latitude, longitude);
            }

            resultStr = prepareCrimeRerpotJSON(crimeReportVO).toString();
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return resultStr;

    }

    private JSONObject prepareCrimeRerpotJSON(CrimeReportVO crimeReportVO) throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(CommonJSONKey.USER_ID_KEY, crimeReportVO.getUserVO().getUserid());
        jobj.put(CommonJSONKey.USER_NAME_KEY, crimeReportVO.getUserVO().getNickName());
        jobj.put(CommonJSONKey.EVENT_ID_KEY, crimeReportVO.getEventId());
        jobj.put(CommonJSONKey.CRIME_NOTE_KEY, crimeReportVO.getNote());
        jobj.put(CommonJSONKey.CRIME_TYPE_KEY, crimeReportVO.getCrimeType());
        jobj.put(CommonJSONKey.LATITUDE_KEY, crimeReportVO.getLatitude());
        jobj.put(CommonJSONKey.LONGITUDE_KEY, crimeReportVO.getLongitude());
        jobj.put(CommonJSONKey.LOCATION_ACCURACY_KEY, crimeReportVO.getAccuracy());
        if (crimeReportVO.getReportDate() != null) {
            jobj.put(CommonJSONKey.CRIME_REPORT_TIME_KEY, crimeReportVO.getReportDate().getTime());
        }
        if (crimeReportVO.getTimeRange() != null) {
            jobj.put(CommonJSONKey.CRIME_TIME_RANGE_KEY, crimeReportVO.getTimeRange());
        }
        if (crimeReportVO.getCrimePictureURL() != null) {
            jobj.put(CommonJSONKey.CRIME_PIC_URL_KEY, crimeReportVO.getCrimePictureURL());
        }
        if (crimeReportVO.getTimeZone() != null) {
            jobj.put(CommonJSONKey.CRIME_TIME_ZONE_KEY, crimeReportVO.getTimeZone());
        }

        return jobj;
    }

    private JSONArray prepareCrimeReportArray(List<CrimeReportVO> reportVOList) throws JSONException {
        JSONArray crimeArray = new JSONArray();
        for (CrimeReportVO crimeReportVO : reportVOList) {
            crimeArray.put(prepareCrimeRerpotJSON(crimeReportVO));
        }
        return crimeArray;
    }


}

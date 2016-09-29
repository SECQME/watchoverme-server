package com.secqme.domain.model.event;

import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.TrackingLogVO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.rs.v2.CommonJSONKey;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * A temporary Value Object on holding SecQmeEventVO, List of TrackingLog + EventLog;
 * User: James Khoo
 * Date: 1/30/14
 * Time: 4:04 PM
 */
public class FullEventInfoVO implements Serializable {
    public static final String TRACKING_LOG_KEY = "trackingLogArray";
    public static final String EVENT_LOG_LIST_KEY = "eventLogArray";
    public static final String LAST_KNOWN_ADDRESS_KEY = "lastKnownAddressKey";

    private static final Logger myLog = Logger.getLogger(FullEventInfoVO.class);

    private SecqMeEventVO secqMeEventVO;
    private List<EventLogVO> eventLogVOList;
    private List<TrackingLogVO> trackingLogVOList;
    private List<String> emergencyVideoURLList;
    private String eventPictureURL;
    private CrimeReportVO crimeReportVO;
    private String lastKnownAddress;

    public FullEventInfoVO() {

    }

    public SecqMeEventVO getSecqMeEventVO() {
        return secqMeEventVO;
    }

    public void setSecqMeEventVO(SecqMeEventVO secqMeEventVO) {
        this.secqMeEventVO = secqMeEventVO;
    }

    public List<EventLogVO> getEventLogVOList() {
        return eventLogVOList;
    }

    public void setEventLogVOList(List<EventLogVO> eventLogVOList) {
        this.eventLogVOList = eventLogVOList;
    }

    public List<TrackingLogVO> getTrackingLogVOList() {
        return trackingLogVOList;
    }

    public void setTrackingLogVOList(List<TrackingLogVO> trackingLogVOList) {
        this.trackingLogVOList = trackingLogVOList;
    }

    public String getLastKnownAddress() {
        return lastKnownAddress;
    }

    public void setLastKnownAddress(String lastKnownAddress) {
        this.lastKnownAddress = lastKnownAddress;
    }

    public CrimeReportVO getCrimeReportVO() {
        return crimeReportVO;
    }

    public void setCrimeReportVO(CrimeReportVO crimeReport) {
        this.crimeReportVO = crimeReport;
    }

    public List<String> getEmergencyVideoURLList() {
        return emergencyVideoURLList;
    }

    public void setEmergencyVideoURLList(List<String> emergencyVideoURLList) {
        this.emergencyVideoURLList = emergencyVideoURLList;
    }

    public String getEventPictureURL() {
        return eventPictureURL;
    }

    public void setEventPictureURL(String eventPictureURL) {
        this.eventPictureURL = eventPictureURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FullEventInfoVO that = (FullEventInfoVO) o;

        if (!secqMeEventVO.equals(that.secqMeEventVO)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return secqMeEventVO.hashCode();
    }

    public JSONObject toJSON() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(CommonJSONKey.EVENT_ID_KEY, secqMeEventVO.getId())
                    .put(CommonJSONKey.EVENT_MESSAGE_KEY, secqMeEventVO.getMessage())
                    .put("eventMsg", secqMeEventVO.getMessage())
                    .put(CommonJSONKey.EVENT_TYPE_KEY, secqMeEventVO.getEventType())
                    .put(CommonJSONKey.EVENT_STATUS_KEY, secqMeEventVO.getStatus())
                    .put(CommonJSONKey.TIME_ZONE_KEY, secqMeEventVO.getEventTimeZone())
                    .put(CommonJSONKey.EVENT_START_TIME_KEY, secqMeEventVO.getStartTime().getTime());
            if (secqMeEventVO.getEventDurationInMinutes() != null) {
                jobj.put(CommonJSONKey.EVENT_DURATION_KEY, secqMeEventVO.getEventDurationInMinutes());
            }

            if (secqMeEventVO.getTrackingURL() != null) {
                jobj.put(CommonJSONKey.EVENT_TRACKING_URL, secqMeEventVO.getTrackingURL());
            }

            if (crimeReportVO != null) {
                JSONObject crimeObj = new JSONObject();
                crimeObj.put(CommonJSONKey.CRIME_NOTE_KEY, crimeReportVO.getNote());
                crimeObj.put(CommonJSONKey.CRIME_TYPE_KEY, crimeReportVO.getCrimeType());
                crimeObj.put(CommonJSONKey.LATITUDE_KEY, crimeReportVO.getLatitude());
                crimeObj.put(CommonJSONKey.LONGITUDE_KEY, crimeReportVO.getLongitude());
                crimeObj.put(CommonJSONKey.LOCATION_ACCURACY_KEY, crimeReportVO.getAccuracy());
                if (crimeReportVO.getReportDate() != null) {
                    crimeObj.put(CommonJSONKey.CRIME_REPORT_TIME_KEY, crimeReportVO.getReportDate().getTime());
                }
                jobj.put(CommonJSONKey.CRIME_REPORT_KEY, crimeObj);
            }

            Boolean userConfirmSafety = (secqMeEventVO != null && (EventStatusType.END == secqMeEventVO.getStatus())
                    || EventStatusType.SAFE_NOTIFY == secqMeEventVO.getStatus());

            jobj.put(SecqMeEventVO.USER_CONFIRM_SAFETY_KEY, userConfirmSafety);
            if (userConfirmSafety) {
                jobj.put(SecqMeEventVO.CONFIRM_SAFETY_TIME_KEY, secqMeEventVO.getConfirmSafetyTime().getTime());
            }
            jobj.put(CommonJSONKey.TRACKING_PIN_KEY, secqMeEventVO.getTrackingPin());
            // Getting all the TrackingLog
            if (trackingLogVOList != null && trackingLogVOList.size() > 0) {
                JSONArray trackingArray = new JSONArray();
                for (TrackingLogVO trackingLogVO : trackingLogVOList) {
                    trackingArray.put(trackingLogVO.toJSON());
                }
                jobj.put(TRACKING_LOG_KEY, trackingArray);

                if (lastKnownAddress != null) {
                    jobj.put(LAST_KNOWN_ADDRESS_KEY, this.getLastKnownAddress());
                }
            }


            if (eventLogVOList != null && eventLogVOList.size() > 0) {
                JSONArray eventLogArray = new JSONArray();
                for (EventLogVO eventLog : eventLogVOList) {
                    eventLogArray.put(eventLog.toJSON());
                }
                jobj.put(EVENT_LOG_LIST_KEY, eventLogArray);
            }

            if(eventPictureURL != null) {
                jobj.put(CommonJSONKey.EMERGENCY_PICTURE_KEY, eventPictureURL);
            }

            if(emergencyVideoURLList != null) {
                JSONArray videoArray = new JSONArray();
                for(String videoURL : emergencyVideoURLList) {
                    videoArray.put(videoURL);
                }
                jobj.put(CommonJSONKey.EMERGENCY_VIDEO_LIST_KEY, videoArray);
            }

            //set event user mobile number
            if(secqMeEventVO.getUserVO().getMobileCountry() != null &&
            		secqMeEventVO.getUserVO().getMobileNo() != null) {
            	jobj.put(CommonJSONKey.MOBILE_NO_KEY, secqMeEventVO.getUserVO().getMobileCountry().getCallingCode()
            			+ secqMeEventVO.getUserVO().getMobileNo());
            }
            
        } catch (JSONException ex) {
            myLog.error("Failed to create JSONObject from: " + this, ex);
        }

        return jobj;
    }
}

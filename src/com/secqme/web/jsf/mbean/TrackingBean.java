package com.secqme.web.jsf.mbean;

import com.secqme.CoreException;
import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.LanguageVO;
import com.secqme.domain.model.TrackingLogVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.manager.EventManager;
import com.secqme.manager.TrackingManager;
import com.secqme.util.LocaleManager;
import com.secqme.util.LocationUtil;
import com.secqme.util.MediaFileType;
import com.secqme.util.io.MediaFileManager;
import com.secqme.web.jsf.util.MessageController;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * @author jameskhoo
 */
public class TrackingBean implements Serializable {

    private final static Logger myLog = Logger.getLogger(TrackingBean.class);
    private static final String defaultTimeZoneStr = "GMT";
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM-dd hh:mm aaa");
    private TrackingManager trackingManager = null;
    private EventManager eventManager = null;
    private LocaleManager localeManager = null;
    private String trackingPin = null;
    private List<TrackingLogVO> trackingLogVOList = null;
    private SecqMeEventVO currentEventVO = null;
    private MapVO theMapVO = null;
    private String trackingLogStartingAddr = null;
    private String trackingLogCurrentAddr = null;
    private String audioURL = null;
    private String picURL = null;
    private MediaFileManager mediaFileManager = null;
    private String timezoneStr = null;
    private String currentEmergencyAudioURL = null;
    private String currentEmergencyVideoURL = null;
    private TrackingLogVO startingTrackingLogVO = null;
    private TrackingLogVO currentTrackingLogVO = null;
    private SelectItem[] emergencyAudioItems = null;
    private SelectItem[] emergencyVideoItems = null;
    // A simple work around on handling timeZone change
    private boolean handleTimeZoneChange = false;
    private boolean handleEmergencyVoiceChange = false;
    private boolean handleEmergencyVideoChange = false;
    private boolean eventHasLogMessage = false;
    private String eventLogMessage = null;
    private LanguageVO currentLanguageVO;
    private String eventMessage = null;
    // JK 18-Jan-2013 
    // Added client timezonestr, will use JavaScript to push this over
    private String clientTimeZoneStr = null;
    private String eventRegTime;
    private String confirmSafetyTime;
    private String currentTrackingTime;
    private String startingTrackingTime;
    private LocationUtil locationUtil;
    private List<String> videoFileURLList;
    private List<String> pictureFileURLList;

    public TrackingBean() {
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public void setLocaleManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    public double getRadius() {
        Double radius = 0.0;
        if (currentTrackingLogVO != null && currentTrackingLogVO.getAccuracy() != null) {
            radius = currentTrackingLogVO.getAccuracy();
        }
        return radius;
    }

    public boolean isEventHasLogMessage() {
        return eventHasLogMessage;
    }

    public void setEventHasLogMessage(boolean eventHasLogMessage) {
        this.eventHasLogMessage = eventHasLogMessage;
    }

    public String getEventLogMessage() {
        String formattedMessage = null;
        if (currentEventVO != null) {
            formattedMessage = eventLogMessage.replaceAll("(\r\n|\n)", "<br />");
        }

        return formattedMessage;
    }

    public void setEventLogMessage(String eventLogMessage) {
        this.eventLogMessage = eventLogMessage;
    }

    public LocationUtil getLocationUtil() {
        return locationUtil;
    }

    public void setLocationUtil(LocationUtil locationUtil) {
        this.locationUtil = locationUtil;
    }

    public String getClientTimeZoneStr() {
        return clientTimeZoneStr;
    }

    public void setClientTimeZoneStr(String clientTimeZoneStr) {
        this.clientTimeZoneStr = clientTimeZoneStr;
    }

    public String getCurrentLocStr() {
        String currentLocStr = null;
        if (currentTrackingLogVO != null) {
            currentLocStr = currentTrackingLogVO.getLocString();
        }
        return currentLocStr;
    }

    public String getMapCenterLocStr() {
        String mapCenterLocStr = null;
        if(currentTrackingLogVO != null) {
            Double latitude = currentTrackingLogVO.getLatitude();
            Double longitude = currentTrackingLogVO.getLongitude();

            mapCenterLocStr =  latitude + ", " + longitude;
        }
        return mapCenterLocStr;
    }

    public List<String> getVideoFileURLList() {
        return videoFileURLList;
    }

    public void setVideoFileURLList(List<String> videoFileURLList) {
        this.videoFileURLList = videoFileURLList;
    }

    public List<String> getPictureFileURLList() {
        return pictureFileURLList;
    }

    public void setPictureFileURLList(List<String> pictureFileURLList) {
        this.pictureFileURLList = pictureFileURLList;
    }

    public TrackingManager getTrackingManager() {
        return trackingManager;
    }

    public void setTrackingManager(TrackingManager trackingManager) {
        this.trackingManager = trackingManager;
    }

    public List<TrackingLogVO> getTrackingLogVOList() {
        return trackingLogVOList;
    }

    public void setTrackingLogVOList(List<TrackingLogVO> trackingLogVOList) {
        this.trackingLogVOList = trackingLogVOList;
    }

    public LanguageVO getCurrentLanguageVO() {
        return currentLanguageVO;
    }

    public void setCurrentLanguageVO(LanguageVO currentLanguageVO) {
        this.currentLanguageVO = currentLanguageVO;
    }

    public boolean isMoreThenOneEmergencyVideos() {
        boolean moreThenOneVideo = false;
        if (emergencyVideoItems != null && emergencyVideoItems.length > 1) {
            moreThenOneVideo = true;
        }
        return moreThenOneVideo;
    }

    public String getCurrentEmergencyVideoURLWithWEBMFormat() {
        return getCurrentEmergencyVideoURL().replace("mp4", "webm");

    }

    public String getCurrentEmergencyVideoURLwithOGVFormat() {
        return getCurrentEmergencyVideoURL().replace("mp4", "ogv");
    }

    public String getTimezoneStr() {
        String formatedStr = null;
        if (timezoneStr != null) {
            formatedStr = timezoneStr.replaceAll("/", "-").replaceAll("_", " ");
        }
        return formatedStr;
    }

    public void setTimezoneStr(String timezoneStr) {
        this.timezoneStr = timezoneStr;
    }

    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public MediaFileManager getMediaFileManager() {
        return mediaFileManager;
    }

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    public SecqMeEventVO getSecqMeEventVO() {
        return currentEventVO;
    }

    public void setSecqMeEventVO(SecqMeEventVO secqMeEventVO) {
        this.currentEventVO = secqMeEventVO;
    }

    public String getCurrentEmergencyAudioURL() {
        return currentEmergencyAudioURL;
    }

    public void setCurrentEmergencyAudioURL(String currentEmergencyAudioURL) {
        this.currentEmergencyAudioURL = currentEmergencyAudioURL;
    }

    public SelectItem[] getEmergencyAudioItems() {
        return emergencyAudioItems;
    }

    public void setEmergencyAudioItems(SelectItem[] emergencyAudioItems) {
        this.emergencyAudioItems = emergencyAudioItems;
    }

    public String getFormatedEventMessage() {
        String formattedMessage = null;
        if (currentEventVO != null && eventMessage != null) {
            formattedMessage = eventMessage.replaceAll("(\r\n|\n)", "<br />");
        }

        return formattedMessage;

    }

    public String getAudioURL() {
        return audioURL;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public String getTrackingLogCurrentAddr() {
        return trackingLogCurrentAddr;
    }

    public String getTrackingLogStartingAddr() {
        return trackingLogStartingAddr;
    }

    public String getTrackingPin() {
        return trackingPin;
    }

    public void setTrackingPin(String trackingPin) {
        this.trackingPin = trackingPin;
    }

    public String getStartingLocStr() {
        return startingTrackingLogVO != null ? startingTrackingLogVO.getLocString() : null;
    }


    public MapVO getMapVO() {
        return theMapVO;
    }

    public Boolean getRenderWatchMeButton() {
        return (!getRenderHelpMeButton() && !getRenderIamOKButton() && currentEventVO != null);
    }

    public Boolean getRenderHelpMeButton() {
        Boolean eventNotify = false;

        if (currentEventVO != null && EventStatusType.NOTIFY == currentEventVO.getStatus()) {
            eventNotify = true;
        }

        return eventNotify;
    }

    public String getEventRegTime() {
        return eventRegTime;
    }

    public void setEventRegTime(String eventRegTime) {
        this.eventRegTime = eventRegTime;
    }

    public String getConfirmSafetyTime() {
        return confirmSafetyTime;
    }

    public void setConfirmSafetyTime(String confirmSafetyTime) {
        this.confirmSafetyTime = confirmSafetyTime;
    }

    public String getCurrentTrackingTime() {
        return currentTrackingTime;
    }

    public void setCurrentTrackingTime(String currentTrackingTime) {
        this.currentTrackingTime = currentTrackingTime;
    }

    public String getStartingTrackingTime() {
        return startingTrackingTime;
    }

    public void setStartingTrackingTime(String startingTrackingTime) {
        this.startingTrackingTime = startingTrackingTime;
    }

    public Boolean getRenderIamOKButton() {
        Boolean confirmSafety = false;

        if (currentEventVO != null && (EventStatusType.END == currentEventVO.getStatus()
                || EventStatusType.SAFE_NOTIFY == currentEventVO.getStatus() )) {
            confirmSafety = true;
        }

        return confirmSafety;
    }

    public void initTrackingDetail() {
        myLog.debug("GetTrackingLog->" + trackingPin + ", clientTimeZoneStr->" + clientTimeZoneStr);
        if (trackingPin == null) {
            return;
        }

        if (handleTimeZoneChange || handleEmergencyVoiceChange || handleEmergencyVideoChange) {
            handleTimeZoneChange = false;
            handleEmergencyVoiceChange = false;
            handleEmergencyVideoChange = false;
            if (handleEmergencyVideoChange) {
                myLog.debug("currentEmergencyVideoURL->" + currentEmergencyVideoURL);
            }
            myLog.debug("Handle timezone or emergency or video change");
            return;
        }

        trackingLogVOList = null;
        currentEventVO = null;
        theMapVO = null;
        trackingLogCurrentAddr = null;
        trackingLogStartingAddr = null;
        audioURL = null;
        picURL = null;
        emergencyVideoItems = null;
        emergencyAudioItems = null;
        currentLanguageVO = null;
        startingTrackingLogVO = null;
        currentTrackingLogVO = null;
        eventMessage = null;
        eventHasLogMessage = false;
        eventLogMessage = null;
        videoFileURLList = null;

        try {
            currentEventVO = eventManager.getEventByTrackingPin(trackingPin);
//            if (currentLanguageVO == null) {
//                currentLanguageVO = localeManager.getLanguageVO(currentEventVO.getUserVO().getLangCode());
//                FacesContext.getCurrentInstance()
//                        .getViewRoot().setLocale(localeManager.getLocale(currentEventVO.getUserVO().getLangCode()));
//            }

            trackingLogVOList = trackingManager.getAllTrackingLog(currentEventVO.getId(), true);
            myLog.debug("Total " + trackingLogVOList.size() + " tracking Log Record Found!");
            if (trackingLogVOList != null && trackingLogVOList.size() > 0) {
                // simpleMapVO = new MapVO(trackingLogVOList, MapType.Simple);
                theMapVO = new MapVO(trackingLogVOList, MapType.Complex);
                startingTrackingLogVO = trackingLogVOList.get(trackingLogVOList.size() - 1);
                currentTrackingLogVO = trackingLogVOList.get(0);
                trackingLogCurrentAddr = locationUtil.getStreetAddr(currentTrackingLogVO.getLatitude(), currentTrackingLogVO.getLongitude());
                trackingLogStartingAddr = locationUtil.getStreetAddr(startingTrackingLogVO.getLatitude(), startingTrackingLogVO.getLongitude());
                currentTrackingTime = dateTimeFormat.format(currentTrackingLogVO.getTimeReport());
                startingTrackingTime = dateTimeFormat.format(startingTrackingLogVO.getTimeReport());
            }

            // To determine the final time zone to use
            // the priority
            // 1.- Use the eventVO timeZoneStr if available
            // 2.- Use Client timeZoneStr
            // 3. - Use the Default TimeZoneStr 

            myLog.debug("Before decide timeZone, currentEvent TimeZone:" + currentEventVO.getEventTimeZone());
            if (currentEventVO.getEventTimeZone() != null) {
                timezoneStr = currentEventVO.getEventTimeZone();
            } else if (clientTimeZoneStr != null && timezoneStr == null) {
                timezoneStr = clientTimeZoneStr;
                myLog.debug("Using client TimeZone->" + clientTimeZoneStr);
            } else {
                timezoneStr = defaultTimeZoneStr;
                myLog.debug("Using Default TimeZone->" + defaultTimeZoneStr);
            }

            dateTimeFormat.setTimeZone(TimeZone.getTimeZone(timezoneStr));


            eventRegTime = dateTimeFormat.format(currentEventVO.getStartTime());
            myLog.debug("Getting the time Zone of->" + dateTimeFormat.getTimeZone().getDisplayName() + ", eventRegTime->" + eventRegTime);
            if (currentEventVO.getConfirmSafetyTime() != null) {
                confirmSafetyTime = dateTimeFormat.format(currentEventVO.getConfirmSafetyTime());
            }

            if (currentEventVO.getMessage().indexOf("Received emergency request from user") > 0) {
                //This event is a merge event.. 
                eventHasLogMessage = true;
                int msgIndex = currentEventVO.getMessage().indexOf("Received emergency request from user");
                eventMessage = currentEventVO.getMessage().substring(0, msgIndex);
                eventLogMessage = currentEventVO.getMessage().substring(msgIndex);
            } else {
                eventMessage = currentEventVO.getMessage();
            }

            if (currentEventVO.getEnableAudio()) {
                if (currentEventVO.getEnableAudio()) {
                    List<String> urls = getMediaFileManager().populateEventMediaUrl(currentEventVO, MediaFileType.AUDIO);
                    if (!urls.isEmpty()) {
                        audioURL = urls.get(0);
                    }
                }
                List<String> audioFileURLList = mediaFileManager.populateEventMediaUrl(currentEventVO, MediaFileType.AUDIO);

                if (audioFileURLList != null && audioFileURLList.size() > 0) {
                    emergencyAudioItems = new SelectItem[audioFileURLList.size()];
                    int index = 0;
                    for (String audioFileURL :  audioFileURLList) {
                        int number = index + 1;
                        emergencyAudioItems[index] = new SelectItem(audioFileURL, "" + number + ". AUDIO" );
                        index++;
                    }
                }

                if (emergencyAudioItems != null && emergencyAudioItems.length > 0) {
                    currentEmergencyAudioURL = (String) emergencyAudioItems[0].getValue();
                }
            }


            if (currentEventVO.getEnableVideo()) {
                videoFileURLList = mediaFileManager.populateEventMediaUrl(currentEventVO, MediaFileType.VIDEO);
                if(videoFileURLList != null && videoFileURLList.size() > 0) {
                    emergencyVideoItems = new SelectItem[videoFileURLList.size()];
                    int index = 0;
                    for(String videoFileURL :  videoFileURLList) {
                        int number = index +1;
                        emergencyVideoItems[index] = new SelectItem(videoFileURL, "" + number + ". VIDEO" );
                        index++;
                    }
                }
                if (emergencyVideoItems != null && emergencyVideoItems.length > 0) {
                    currentEmergencyVideoURL = (String) emergencyVideoItems[0].getValue();
                }
            }

            if (currentEventVO.getEnablePicture()) {
                pictureFileURLList = getMediaFileManager().populateEventMediaUrl(currentEventVO, MediaFileType.PICTURE);
                if (!pictureFileURLList.isEmpty()) {
                    picURL = pictureFileURLList.get(pictureFileURLList.size() - 1);
                }
            }

            myLog.debug("Tracking with pin:" + trackingPin + " is audio Enable->" + currentEventVO.getEnableAudio()
                    + " endTime->" + currentEventVO.getEndTime()
                    + ", duration->" + currentEventVO.getEventDurationInMinutes());

        } catch (CoreException ex) {
            myLog.error("Error on tracking with pin :" + trackingPin + ", reason:" + ex.getMessage(), ex);
            MessageController.addCoreExceptionError(null, ex);
        }
    }

    public void changeEmergencyVoiceURL() {
        myLog.debug("Current emergency VOICE URL is->" + currentEmergencyAudioURL);
        handleEmergencyVoiceChange = true;
    }

    public void changeEmergencyVideoURL() {
        myLog.debug("Current emergency VIDEO URL is->" + currentEmergencyVideoURL);
        handleEmergencyVideoChange = true;
    }

    public String getCurrentEmergencyVideoURL() {
        return currentEmergencyVideoURL;
    }

    public void setCurrentEmergencyVideoURL(String currentEmergencyVideoURL) {
        this.currentEmergencyVideoURL = currentEmergencyVideoURL;
    }

    public SelectItem[] getEmergencyVideoItems() {
        return emergencyVideoItems;
    }

    public void setEmergencyVideoItems(SelectItem[] emergencyVideoItems) {
        this.emergencyVideoItems = emergencyVideoItems;
    }
}

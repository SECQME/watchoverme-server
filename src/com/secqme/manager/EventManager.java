package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.domain.model.event.FullEventInfoVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.event.ShareEventVO;
import com.secqme.domain.model.UserVO;
import com.secqme.util.MediaFileType;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public interface EventManager {

    public SecqMeEventVO updateEventLog(Long eventId, String newEventMsg, String encodedMediaStr, MediaFileType mediaFileType,
                                       Double latitude, Double longitude, Double accuracy) throws CoreException;
    public SecqMeEventVO registerNewEvent(SecqMeEventVO secqMeEvent,
                                          String encodedPicStr, String encodedAudioStr,
                                          String encodedVideoStr) throws CoreException;

    public SecqMeEventVO registerTryEvent(Integer eventDurationInMinutes) throws CoreException;
    public SecqMeEventVO mergeEmergencyVideoIntoCurrentEvent(Long eventID, SecqMeEventVO emergencyEventWithVideo,
                                                             String emergencyAudioStream, InputStream currentVideoStream,
                                                             Boolean triggerNotification) throws CoreException;
    public SecqMeEventVO registerNewEvent(SecqMeEventVO secqMeEvent,
                                          String encodedPicStr, String encodedAudioStr,
                                          String encodedVideoStr, Boolean triggerNotification) throws CoreException;
    public void confirmSafetyForUserLatestEvent(UserVO userVO, Boolean sendSafelyArrivalNotification) throws CoreException;
    public void confirmSafetyForUserLatestEvent(UserVO userVO, Boolean sendSafelyArrivalNotification,
    		Double latitude, Double longitude, Double accuracy) throws CoreException;
    public void processExpiredEvent(SecqMeEventVO event) throws CoreException;
    public void processExpireEventList(List<SecqMeEventVO> eventList) throws CoreException;
    public void processEmergencyEvent(SecqMeEventVO secqMeEvent) throws CoreException;
    public SecqMeEventVO getUserLatestEvent(String userid);
    public List<SecqMeEventVO> getUserCurrentRunningEvents(UserVO userVO);
    public SecqMeEventVO getEventByTrackingPin(String trackingPin) throws CoreException;
    public List<SecqMeEventVO> getUserEvents(String userid, Date startDate, Date endDate) throws CoreException;
    public boolean extendCurrentEvent(UserVO userVO, long extendedDuration) throws CoreException;
    public boolean updateSNSManually(SecqMeEventVO secqMeEventVO);
    public FullEventInfoVO getFullEventInfo(UserVO userVO, Long eventID) throws CoreException;
    public Long getUserTotalRegisteredEvent(String userid) throws CoreException;
    public void notifyContactsOnSafelyArrival(UserVO userVO, boolean  toSendSMS, boolean toSendEmail, boolean toSendfacebook);
    public SecqMeEventVO findEventByEventID(Long eventID) throws CoreException;
    public void notifyTrialEvent(Long trialEventID, String mobileCountryISO, String mobileNumber, String langCode) throws CoreException;
    public List<SecqMeEventVO> getUserEventHistory(UserVO userVO, Integer startingRecord, Integer maxRecord) throws CoreException;
    public FullEventInfoVO getFullEventInfoOfContact(Long eventID) throws CoreException;
    public void shareEvent(UserVO userVO, Long eventId, List<Long> contactList) throws CoreException;
    public ShareEventVO getShareEventVO(String shareToUserid, Long eventid);
    public List<ShareEventVO> getEventShareToUserWithinADay(UserVO userVO);
}

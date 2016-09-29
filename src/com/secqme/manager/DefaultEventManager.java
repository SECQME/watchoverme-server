package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.*;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.event.EventLogVO;
import com.secqme.domain.model.event.EventType;
import com.secqme.domain.model.event.FullEventInfoVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.event.ShareEventVO;
import com.secqme.manager.billing.BillingManager;
import com.secqme.manager.worker.NotifyExpiredEventWorker;
import com.secqme.manager.worker.ShortURLWorker;
import com.secqme.util.MediaFileType;
import com.secqme.util.ShortURLUpdateService;
import com.secqme.util.TimeZoneUpdateService;
import com.secqme.util.notification.PushNotificationEventType;
import com.secqme.util.shorturl.ShortURLException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author james
 */
public class DefaultEventManager extends BaseManager implements EventManager, TimeZoneUpdateService, ShortURLUpdateService, Serializable {

    private static final Logger myLog = Logger.getLogger(DefaultEventManager.class);
    private final static Long MILLISECONDS_IN_A_MINUTE = 60l * 1000;
    private final BillingManager billingManager;
    private final ExecutorService threadPool;
    private final String trackingBaseURL;
    private List<String> demoUserIDList;
//    private QuickEventManager quickEventManager;

    public DefaultEventManager(BillingManager billingMgr,
                               String trackingBaseURL, List<String> demoUserIDList) {
        billingManager = billingMgr;
//        this.quickEventManager = quickEventManager;
        threadPool = Executors.newCachedThreadPool();
        this.trackingBaseURL = trackingBaseURL;
        this.demoUserIDList = demoUserIDList;
    }

    public SecqMeEventVO registerTryEvent(Integer eventDurationInMinutes) throws CoreException {
        // use the List of user to register the Trial Event base on the Time
        Date startTime = new Date();
        Date endTime = DateUtils.addMinutes(startTime, eventDurationInMinutes);
        Long index = (startTime.getTime() % demoUserIDList.size());
        String trialUserID = this.demoUserIDList.get(index.intValue());
        UserVO userVO = getUserDAO().read(trialUserID);
        myLog.debug("Register trial event via demo User->" + userVO.getUserid());
        SecqMeEventVO eventVO = new SecqMeEventVO();
        eventVO.setMessage("Watch Over Me");
        eventVO.setUserVO(userVO);
        eventVO.setEventType(EventType.NORMAL);
        eventVO.setEnableGPS(true);
        eventVO.setStartTime(startTime);
        eventVO.setEndTime(endTime);
        eventVO.setStatus(EventStatusType.NEW);
        String trackingPin = getTextUtil().generateRandomString(6, 7);
        eventVO.setTrackingPin(trackingPin);
        try {
            String trackingURL = getUrlShortenerService().getShortURL(trackingBaseURL + trackingPin, "tracking");
            eventVO.setTrackingURL(trackingURL);
        } catch (ShortURLException ex) {
            myLog.error("Failed to generate short url for: " + trackingBaseURL + trackingPin, ex);
        }

        getSecqMeEventDAO().create(eventVO);
        insertEventLogRecord(eventVO, EventStatusType.NEW, "Received", null, null, null);
        getScheduleManager().scheduleCheckExpireEventJob(eventVO);
        return eventVO;
    }

    public SecqMeEventVO registerNewEvent(SecqMeEventVO currentEvent,
                                          String encodedPicStream,
                                          String encodedAudioStream,
                                          String encodedVideoStream) throws CoreException {
        return registerNewEvent(currentEvent, encodedPicStream, encodedAudioStream, encodedVideoStream, null);
    }

    public SecqMeEventVO registerNewEvent(SecqMeEventVO currentEvent,
                                          String encodedPicStream,
                                          String encodedAudioStream,
                                          String encodedVideoStream,
                                          Boolean triggerNotification) throws CoreException {

        UserVO userVO = currentEvent.getUserVO();
        billingManager.authorizeTransaction(currentEvent);

        // JK 16th APR-2013
        // Change of business logic
        // User are always require to end the event from their phone,
        // thus in regardless if the event is emergency event, or 
        // just a normal event, always merge those event together

        SecqMeEventVO userLatestEvent = getSecqMeEventDAO().getUserLatestEvent(userVO.getUserid());
        if (userLatestEvent != null) {
            myLog.debug(userVO.getUserid() + " latestEvent status->" + userLatestEvent.getStatus()
                    + ",latestEvenType->" + userLatestEvent.getEventType()
                    + ", latestEvent.endTime->" + userLatestEvent.getEndTime()
                    + ", currentEvent.startTime->" + currentEvent.getStartTime());

//            if (EventType.EMERGENCY.equals(currentEvent.getEventType())
//                    && !EventStatusType.END.equals(userLatestEvent.getStatus())
//                    && userLatestEvent.getEndTime() != null
//                    && userLatestEvent.getEndTime().getTime() > currentEvent.getStartTime().getTime()) {
            if (EventType.EMERGENCY.equals(currentEvent.getEventType())
                    && !EventStatusType.END.equals(userLatestEvent.getStatus())) {
                myLog.info("Merging event for user->" + userLatestEvent.getUserVO().getUserid());
                SecqMeEventVO mergeEventVO =
                        mergeCurrentEvent(userLatestEvent, currentEvent, encodedAudioStream, encodedVideoStream, null, triggerNotification);
                billingManager.chargeEventRegistration(mergeEventVO);

                // As the event has turn to Emergency, remove the job if any;
                getScheduleManager().removeExpireEventJob(mergeEventVO);
                return mergeEventVO;
            }
        }


        myLog.info("Creating new event for user->" + userVO.getUserid());

        // Create the new SecqMeEvent
        if (currentEvent.getStatus() == null) {
            currentEvent.setStatus(EventStatusType.NEW);
        }
        //
        confirmSafetyForUserLatestEvent(userVO, false);
        //
        String trackingPin = getTextUtil().generateRandomString(6, 7);
        currentEvent.setTrackingPin(trackingPin);

        if (EventType.EMERGENCY.equals(currentEvent.getEventType())) {
            currentEvent.setEmergencyTriggerAt(currentEvent.getStartTime());
        }
        getSecqMeEventDAO().create(currentEvent);

        // TODO: Fix race condition of generating short URL
        // Better if we can send notification just after short URL already generated.
        ShortURLWorker shortURLWorker = new ShortURLWorker(this, currentEvent, trackingBaseURL + trackingPin);
        FutureTask ft = new FutureTask(shortURLWorker);
        threadPool.submit(ft);


        // Prepare media file to be saved
        // Only save one file. Media type priority: video, picture, and then audio.
        MediaFileType mediaFileType = null;
        String encodedMediaFile = null;
        if (encodedVideoStream != null) {
            mediaFileType = MediaFileType.VIDEO;
            encodedMediaFile = encodedVideoStream;
            currentEvent.setEnableVideo(true);
        } else if (encodedPicStream != null) {
            mediaFileType = MediaFileType.PICTURE;
            encodedMediaFile = encodedPicStream;
            currentEvent.setEnablePicture(true);
        } else if (encodedAudioStream != null) {
            mediaFileType = MediaFileType.AUDIO;
            encodedMediaFile = encodedAudioStream;
            currentEvent.setEnableAudio(true);
        }

        if (encodedMediaFile != null) {
            insertEventLogRecord(currentEvent, EventStatusType.NEW, "Received", null, null, null, mediaFileType, encodedMediaFile);
        } else {
            insertEventLogRecord(currentEvent, EventStatusType.NEW, "Received", null, null, null);
        }

        if (EventType.EMERGENCY == currentEvent.getEventType()) {
            processEmergencyEvent(currentEvent);
        }

        billingManager.chargeEventRegistration(currentEvent);

        getSecqMeEventDAO().update(currentEvent);
        getUserDAO().update(userVO);

        if (EventStatusType.NEW.equals(currentEvent.getStatus())) {
            getScheduleManager().scheduleCheckExpireEventJob(currentEvent);
        }
        // push message to user added contact
        if (EventType.EMERGENCY == currentEvent.getEventType()) {
            if (userVO.getContactList() != null && userVO.getContactList().size() > 0) {
				for (ContactVO contactVO : userVO.getContactList()) {
					if (isAbleToReceivePushNotification(contactVO)) {
						getNotificationEngine().sendEventPushNotification(userVO, contactVO.getContactUserVO(),
								PushNotificationEventType.EMERGENCY_EVENT, currentEvent);
					}
				}
			}
			
        }
        return currentEvent;
    }

    /**
     * Todo here
     *
     * @param eventId
     * @param newEventMsg
     * @param encodedMediaStr
     * @param latitude
     * @param longitude
     * @param accuracy
     * @return
     * @throws CoreException
     */
    public SecqMeEventVO updateEventLog(Long eventId, String newEventMsg, String encodedMediaStr, MediaFileType mediaFileType,
                                        Double latitude, Double longitude, Double accuracy) throws CoreException {
        SecqMeEventVO eventVO = getSecqMeEventDAO().read(eventId);
        String eventLogMediaFileURL = null;
        if (eventVO != null) {
            if (newEventMsg != null) {
                eventVO.setMessage(newEventMsg);
            }
            if (encodedMediaStr != null) {
                if (MediaFileType.AUDIO.equals(mediaFileType)) {
                    eventVO.setEnableAudio(true);
                } else if (MediaFileType.PICTURE.equals(mediaFileType)) {
                    eventVO.setEnablePicture(true);
                } else if (MediaFileType.VIDEO.equals(mediaFileType)) {
                    eventVO.setEnableVideo(true);
                }
            }
            getSecqMeEventDAO().update(eventVO);
            insertEventLogRecord(eventVO, EventStatusType.JOURNEY, newEventMsg,
                    latitude, longitude, accuracy, mediaFileType, encodedMediaStr);

        } else {
            throw new CoreException(ErrorType.EVENT_NOT_FOUND_BY_EVENT_ID, eventId.toString());
        }

        return eventVO;
    }

    public List<SecqMeEventVO> getUserCurrentRunningEvents(UserVO userVO) {
        return getSecqMeEventDAO().findNewConfirmEventByUser(userVO.getUserid());
    }

    public Long getUserTotalRegisteredEvent(String userid) throws CoreException {
        return getSecqMeEventDAO().getNumberOfEventRegistered(userid);
    }

    public void processEmergencyEvent(SecqMeEventVO secqMeEvent) throws CoreException {
        //int availbleSMSCredit = billingManager.getAvailableSMSCredit(secqMeEvent.getUserVO());
        updateShortUrlIfNeeded(secqMeEvent);

        int availableSMSCredit = billingManager.getAvailableSMSCredit(secqMeEvent);
        myLog.debug("Processing Emergency Event for: " + secqMeEvent.getUserVO().getUserid() + "  (SMS credit: " + availableSMSCredit + ")");
        int actualSMSSend = getNotificationEngine().notifyEmergencyEvent(secqMeEvent, availableSMSCredit);
        insertEventLogRecord(secqMeEvent, EventStatusType.NOTIFY,
                "Notifying user's contact, total of " + actualSMSSend + " sms send to contacts", null, null, null);
        secqMeEvent.setStatus(EventStatusType.NOTIFY);
        billingManager.chargeSMSCreditUsed(secqMeEvent, actualSMSSend);
        getUserDAO().update(secqMeEvent.getUserVO());
        getSecqMeEventDAO().update(secqMeEvent);
    }

    public void processExpireEventList(List<SecqMeEventVO> eventList) throws CoreException {
        if (eventList != null && eventList.size() > 0) {
            myLog.debug("Processing total of " + eventList.size() + " expired events");
            for (SecqMeEventVO secqMeEvent : eventList) {
                threadPool.submit(new NotifyExpiredEventWorker(this, secqMeEvent));
            }
        }
    }

    public void processExpiredEvent(SecqMeEventVO secqMeEventVO) throws CoreException {
        updateShortUrlIfNeeded(secqMeEventVO);

        // find out number of SMS Credit to send
        //int availbleSMSCredit = billingManager.getAvailableSMSCredit(event.getUserVO());
        int availbleSMSCredit = billingManager.getAvailableSMSCredit(secqMeEventVO);
        myLog.debug("Processing Expire Event for:" + secqMeEventVO.getUserVO().getUserid() + " sms Credit:" + availbleSMSCredit);
        int actualSMSNotify =
                getNotificationEngine().notifyExpireEvent(secqMeEventVO, availbleSMSCredit);
        billingManager.chargeSMSCreditUsed(secqMeEventVO, actualSMSNotify);
        secqMeEventVO.setStatus(EventStatusType.NOTIFY);
        secqMeEventVO.setEnableSafetyNotify(true);
        secqMeEventVO.setEmergencyTriggerAt(new Date());
        secqMeEventVO.setEventType(EventType.EMERGENCY);
        getSecqMeEventDAO().update(secqMeEventVO);
        insertEventLogRecord(secqMeEventVO, EventStatusType.NOTIFY, "Notify user Contacts, update event to emergency", null, null, null);
        getUserDAO().update(secqMeEventVO.getUserVO());
        //send push to contact that user shared event with
//        List<ShareEventVO> shareEventList = getShareEventDAO().findByEventId(event.getId());
//    	if(shareEventList != null && shareEventList.size() > 0) {
//    		UserVO shareToUserVO = null;
//    		for(ShareEventVO shareEventVO : shareEventList) {
//    			if(shareToUserVO == shareEventVO.getShareToUserVO()) {
//    				//skip same share user
//    			} else {
//    				shareToUserVO = shareEventVO.getShareToUserVO();
//    				getNotificationEngine().sendEventPushNotification(event.getUserVO().getNickName(), shareToUserVO, 
//							PushNotificationEventType.EMERGENCY_EVENT, event.getId());
//    			}
//    			
//			}
//    	}
    	//send push to all network contact
    	if (secqMeEventVO.getUserVO().getContactList() != null && secqMeEventVO.getUserVO().getContactList().size() > 0) {
			for (ContactVO contactVO : secqMeEventVO.getUserVO().getContactList()) {
				if (isAbleToReceivePushNotification(contactVO)){
					getNotificationEngine().sendEventPushNotification(secqMeEventVO.getUserVO(), contactVO.getContactUserVO(),
							PushNotificationEventType.EMERGENCY_EVENT, secqMeEventVO);
				}
			}
		}
    }

    public void confirmSafetyForUserLatestEvent(UserVO userVO, Boolean sendSafelyArrivalNotification) throws CoreException {
    	confirmSafetyForUserLatestEvent(userVO, sendSafelyArrivalNotification, null, null, null);
    }
    public void confirmSafetyForUserLatestEvent(UserVO userVO, Boolean sendSafelyArrivalNotification,
    		Double latitude, Double longitude, Double accuracy) throws CoreException {
        myLog.debug("Confirming user latest event" + userVO.getUserid());
        SecqMeEventVO eventVO = getSecqMeEventDAO().getUserLatestEvent(userVO.getUserid());
        if (eventVO != null &&
                EventStatusType.END != eventVO.getStatus() &&
                EventStatusType.SAFE_NOTIFY != eventVO.getStatus()) {
            updateShortUrlIfNeeded(eventVO);

        	//insert user last location
        	if(latitude != null && longitude != null && accuracy != null) {
        		TrackingLogVO trackingLogVO = new TrackingLogVO();
                trackingLogVO.setSecqMeEventVO(eventVO);
                trackingLogVO.setLatitude(latitude);
                trackingLogVO.setLongitude(longitude);
                trackingLogVO.setAccuracy(accuracy);
                trackingLogVO.setTimeReport(new Date());
                getTrackingLogDAO().create(trackingLogVO);
        	}
            eventVO.setStatus(EventStatusType.END);
            eventVO.setConfirmSafetyTime(new Date());
            insertEventLogRecord(eventVO, EventStatusType.END, "Confirm Safety by User at:" + new Date(), null, null, null);

            getScheduleManager().removeExpireEventJob(eventVO);
            getSecqMeEventDAO().update(eventVO);

            // Check if latest event require safety Notification
            if (eventVO.getEnableSafetyNotify() && sendSafelyArrivalNotification) {
                int availbleSMSCredit = billingManager.getAvailableSMSCredit(eventVO);
                myLog.debug("Processing SafetyNotification for: " + userVO.getUserid());
                int actualSMSNotify =
                        getNotificationEngine().notifySafetyConfirmationEvent(eventVO, availbleSMSCredit);
                insertEventLogRecord(eventVO, EventStatusType.SAFE_NOTIFY, "Send Safely Arrival Confirmation to user at:" + new Date(), null, null, null);
                billingManager.chargeSMSCreditUsed(eventVO, actualSMSNotify);
            }
            
            // push message to user added contact
            // normal event
            if(eventVO.getEventType() == EventType.NORMAL && eventVO.isEnableShare()) {
            	List<ShareEventVO> shareEventList = getShareEventDAO().findByEventId(eventVO.getId());
            	if(shareEventList != null && shareEventList.size() > 0) {
            		UserVO shareToUserVO = null;
            		for(ShareEventVO shareEventVO : shareEventList) {
            			if(shareToUserVO == shareEventVO.getShareToUserVO()) {
            				//skip same share user
            			} else {
            				shareToUserVO = shareEventVO.getShareToUserVO();
            				getNotificationEngine().sendEventPushNotification(userVO, shareToUserVO,
        							PushNotificationEventType.NORMAL_EVENT_END, eventVO);
            			}
            			
        			}
            	}
            	
            } else if (eventVO.getEventType() == EventType.EMERGENCY ) { // emergency event
            	if (userVO.getContactList() != null && userVO.getContactList().size() > 0) {
    				for (ContactVO contactVO : userVO.getContactList()) {
    					if (isAbleToReceivePushNotification(contactVO)){
							getNotificationEngine().sendEventPushNotification(userVO, contactVO.getContactUserVO(),
									PushNotificationEventType.EMERGENCY_EVENT_END, eventVO);
    					}
    				}
    			}
            }
            
        }
    }

    private boolean isAbleToReceivePushNotification(ContactVO contactVO) {
        return contactVO.getContactUserVO() != null
                && (ContactInvitationStatus.ACCEPTED == contactVO.getStatus() || ContactInvitationStatus.INVITED == contactVO.getStatus());
    }

    private EventLogVO insertEventLogRecord(SecqMeEventVO secqMeEvent, EventStatusType eventStatusType, String msg,
                                            Double latitude, Double longitude, Double accuracy) {
        EventLogVO eventLogVO = new EventLogVO();
        eventLogVO.setStatus(eventStatusType);
        eventLogVO.setSecqMeEventVO(secqMeEvent);
        eventLogVO.setEventTime(new Date());
        eventLogVO.setLogMessage(msg);
        eventLogVO.setLatitude(latitude);
        eventLogVO.setLongitude(longitude);
        eventLogVO.setAccuracy(accuracy);
        getEventLogDAO().create(eventLogVO);

        return eventLogVO;
    }

    private EventLogVO insertEventLogRecord(SecqMeEventVO secqMeEvent, EventStatusType eventStatusType, String msg,
                                      Double latitude, Double longitude, Double accuracy,
                                      MediaFileType mediaFileType, String encodedMediaFileStr) {
        EventLogVO eventLogVO = insertEventLogRecord(secqMeEvent, eventStatusType, msg, latitude, longitude, accuracy);
        if (mediaFileType != null && encodedMediaFileStr != null) {
            getMediaFileManager().saveEventMediaFile(eventLogVO, mediaFileType, encodedMediaFileStr);
            getEventLogDAO().update(eventLogVO);
        }
        return eventLogVO;
    }

    private EventLogVO insertEventLogRecord(SecqMeEventVO secqMeEvent, EventStatusType eventStatusType, String msg,
                                      Double latitude, Double longitude, Double accuracy,
                                      MediaFileType mediaFileType, InputStream uploadedFile) {
        EventLogVO eventLogVO = insertEventLogRecord(secqMeEvent, eventStatusType, msg, latitude, longitude, accuracy);
        if (mediaFileType != null && uploadedFile != null) {
            getMediaFileManager().saveEventMediaFile(eventLogVO, mediaFileType, uploadedFile);
            getEventLogDAO().update(eventLogVO);
        }
        return eventLogVO;
    }

    public SecqMeEventVO getUserLatestEvent(String userid) {
        return getSecqMeEventDAO().getUserLatestEvent(userid);
    }

    public SecqMeEventVO getEventByTrackingPin(String trackingPin) throws CoreException {
        SecqMeEventVO event = getSecqMeEventDAO().findEventByTrackingPin(trackingPin);
        if (event == null) {
            throw new CoreException(ErrorType.EVENT_NOT_FOUND_FOR_THIS_TRACKING_PIN, null, trackingPin);
        } else if (event.getArchived()) {
            throw new CoreException(ErrorType.EVENT_ARCHIEVED_ERROR, event.getUserVO().getLangCode(), event.getUserVO().getNickName());
        }
        return event;
    }

    public SecqMeEventVO mergeEmergencyVideoIntoCurrentEvent(Long eventID, SecqMeEventVO emergencyEventWithVideo,
                                                             String emergencyAudioStream, InputStream currentVideoStream,
                                                             Boolean triggerNotification) throws CoreException {
        SecqMeEventVO currentEventVO = getSecqMeEventDAO().read(eventID);
        return mergeCurrentEvent(currentEventVO, emergencyEventWithVideo,
                emergencyAudioStream, null, currentVideoStream, triggerNotification);
    }

    private SecqMeEventVO mergeCurrentEvent(SecqMeEventVO userLatestEvent,
                                            SecqMeEventVO currentEmergencyEvent,
                                            String currentEmergencyAudioStream,
                                            String base64VideoString,
                                            InputStream videoInputStream,
                                            Boolean triggerNotification)
            throws CoreException {
        myLog.debug("Merging user : " + currentEmergencyEvent.getUserVO().getUserid()
                + " current emergency Event with user latest running Event..");
        // Forming the event Message
        long durationFromLastEvent = userLatestEvent.getStartTime().getTime()
                - currentEmergencyEvent.getStartTime().getTime();
        String durationStr = DurationFormatUtils.formatDurationWords(durationFromLastEvent, true, true);
        EventStatusType eventStatusType = EventStatusType.EMERGENCY;
        String mediaFilePath = null;

        // Remove all the '-' characters
        durationStr = durationStr.replaceAll("-", "");

//        StringBuffer buf = new StringBuffer();
////        buf.append(userLatestEvent.getMessage());
////        buf.append("\n\n");

        String emergencyMsg = "Received emergency request from user, ";
        if (currentEmergencyAudioStream != null) {
            emergencyMsg = "Received emergency voice message from user, ";
            eventStatusType = EventStatusType.EMERGENCY_AUDIO;
        } else if (base64VideoString != null) {
            emergencyMsg = "Received emergency video from user, ";
            eventStatusType = EventStatusType.EMERGENCY_VIDEO;
        }

//        emergencyMsg = emergencyMsg + durationStr + " after WatchOverMe request submitted";

//        buf.append(emergencyMsg);
//        buf.append(durationStr);
//        buf.append(" after Watch Over Me request submitted.");
//        userLatestEvent.setMessage(buf.toString());
        userLatestEvent.setEventType(currentEmergencyEvent.getEventType());
        userLatestEvent.setEmergencyTriggerAt(new Date());

        //process to notify to user's contact
        if (triggerNotification == null || triggerNotification) {
            processEmergencyEvent(userLatestEvent);
        }

        MediaFileType mediaFileType = null;
        String encodedMediaFile = null;
        if (base64VideoString != null) {
            mediaFileType = MediaFileType.VIDEO;
            encodedMediaFile = base64VideoString;
            userLatestEvent.setEnableVideo(true);
        } else if (currentEmergencyAudioStream != null) {
            mediaFileType = MediaFileType.AUDIO;
            encodedMediaFile = currentEmergencyAudioStream;
            userLatestEvent.setEnableAudio(true);
        }

        if (videoInputStream != null) {
            userLatestEvent.setEnableVideo(true);
            insertEventLogRecord(userLatestEvent, eventStatusType, emergencyMsg, null, null, null, MediaFileType.VIDEO, videoInputStream);
        } else if (encodedMediaFile != null) {
            insertEventLogRecord(userLatestEvent, eventStatusType, emergencyMsg, null, null, null, mediaFileType, encodedMediaFile);
        } else {
            insertEventLogRecord(userLatestEvent, eventStatusType, emergencyMsg, null, null, null);
        }

        getSecqMeEventDAO().update(userLatestEvent);

        return userLatestEvent;
    }

    public List<SecqMeEventVO> getUserEvents(String userid, Date startDate, Date endDate) throws CoreException {
        return getSecqMeEventDAO().findUserEventBetweenDates(userid, startDate, endDate);
    }

    //khlow20120605
    public boolean extendCurrentEvent(UserVO userVO, long extendedDuration) throws CoreException {
        myLog.debug("Extend running event for user:" + userVO.getUserid());
        List<SecqMeEventVO> currentEventList = getSecqMeEventDAO().findNewConfirmEventByUser(userVO.getUserid());
        SecqMeEventVO latestEventVO = null;
        //myLog.debug("event list size:" + currentEventList.size());
        if (currentEventList != null && currentEventList.size() > 0) {
            for (SecqMeEventVO runningEvent : currentEventList) {
                extendCurrentEvent(runningEvent, extendedDuration, "Extend the Event by " + extendedDuration + " minutes, request from user");
            }
            return true;
        } else {
            myLog.debug("running event not found");
            return false;
        }

    }

    private void extendCurrentEvent(SecqMeEventVO eventVO, long extendedDuration, String message) throws CoreException {
        eventVO.setEndTime(new Date(eventVO.getEndTime().getTime() + extendedDuration * MILLISECONDS_IN_A_MINUTE));
        if (eventVO.getExtendedDuration() == null) {
            eventVO.setExtendedDuration(extendedDuration);
        } else {
            eventVO.setExtendedDuration(eventVO.getExtendedDuration() + extendedDuration);
        }
        //eventVO.setStatus(EventStatusType.EXTENDED);
        insertEventLogRecord(eventVO, EventStatusType.EXTENDED, message, null, null, null);
        getSecqMeEventDAO().update(eventVO);

        getScheduleManager().rescheduleExpireEventJob(eventVO);
    }

    public boolean updateSNSManually(SecqMeEventVO secqMeEventVO) {
        getNotificationEngine().sendWatchOverMeNowTOSNS(secqMeEventVO);
        return true;
    }

    public FullEventInfoVO getFullEventInfo(UserVO userVO, Long eventID) throws CoreException {
        FullEventInfoVO fullEventInfoVO = null;
        myLog.debug("GetFullEventInfo" + userVO.getUserid() + " eventID:" + eventID);
        SecqMeEventVO secqMeEventVO = getSecqMeEventDAO().read(eventID);

        if (secqMeEventVO != null && secqMeEventVO.getUserVO().getUserid().equals(userVO.getUserid())) {
            fullEventInfoVO = new FullEventInfoVO();
            fullEventInfoVO.setSecqMeEventVO(secqMeEventVO);

            List<TrackingLogVO> trackingLogList = getTrackingLogDAO().findTrackingLogByEvendId(secqMeEventVO.getId(), Boolean.TRUE);
            fullEventInfoVO.setTrackingLogVOList(trackingLogList);

            List<EventLogVO> eventLogList = getEventLogDAO().findByEventId(secqMeEventVO.getId());
            fullEventInfoVO.setEventLogVOList(eventLogList);

            CrimeReportVO crimeReportVO = getCrimeReportDAO().findCrimeRerpotByEventID(secqMeEventVO.getId());
            fullEventInfoVO.setCrimeReportVO(crimeReportVO);

            if (secqMeEventVO.getEnablePicture()) {
                List<String> urls = getMediaFileManager().populateEventMediaUrl(secqMeEventVO, MediaFileType.PICTURE);
                if (!urls.isEmpty()) {
                    fullEventInfoVO.setEventPictureURL(urls.get(0));
                }
            }

            if (secqMeEventVO.getEnableVideo()) {
                fullEventInfoVO.setEmergencyVideoURLList(getMediaFileManager().populateEventMediaUrl(secqMeEventVO, MediaFileType.VIDEO));
            }

        } else {
            // if the event is not found or the event is not belong to the customer
            myLog.debug("User->" + userVO + ", secqMeEventVO:" + secqMeEventVO);
            throw new CoreException(ErrorType.EVENT_NOT_FOUND_BY_EVENT_ID, eventID.toString());
        }
        return fullEventInfoVO;
    }

    @Override
    public void updateTimeZone(Object valueObject, Double latitude, Double longitude) throws CoreException {
        String timeZoneId = getLocationUtil().getTimeZone(latitude, longitude);
        SecqMeEventVO eventVO = (SecqMeEventVO) valueObject;
        myLog.debug("Event updateTimeZoneService->" + latitude + "," + longitude + " for event->" + eventVO.getId());
        eventVO.setEventTimeZone(timeZoneId);
        getSecqMeEventDAO().update(eventVO);
        UserVO userVO = eventVO.getUserVO();
        userVO.setTimeZone(timeZoneId);
        getUserDAO().update(userVO);
    }

    private void updateShortUrlIfNeeded(SecqMeEventVO eventVO) {
        updateShortURL(eventVO, trackingBaseURL + eventVO.getTrackingPin());
    }

    @Override
    public void updateShortURL(Object valueObject, String longURL) throws CoreException {
        SecqMeEventVO secqMeEventVO = (SecqMeEventVO) valueObject;
        if (StringUtils.isEmpty(secqMeEventVO.getTrackingURL())) {
            myLog.debug("Generate short url for event: " + secqMeEventVO.getId());
            try {
                secqMeEventVO.setTrackingURL(getUrlShortenerService().getShortURL(longURL, "tracking"));
                myLog.debug("Short url for event: " + secqMeEventVO.getId() + " -> " + secqMeEventVO.getTrackingURL());

                getSecqMeEventDAO().update(secqMeEventVO);
            } catch (ShortURLException ex) {
                myLog.error("Failed to generate short url for: " + longURL, ex);
            }
        }
    }

    public void notifyContactsOnSafelyArrival(UserVO userVO, boolean toSendSMS, boolean toSendEmail, boolean toSendfacebook) {
        SecqMeEventVO latestEventVO = null;
        latestEventVO = getSecqMeEventDAO().getUserLatestEvent(userVO.getUserid());
        if (latestEventVO != null) {
            updateShortUrlIfNeeded(latestEventVO);
            int availbleSMSCredit;
            try {
                availbleSMSCredit = billingManager
                        .getAvailableSMSCredit(latestEventVO);
                int actualSMSNotify = getNotificationEngine()
                        .notifyContactOnSafelyArrival(latestEventVO,
                                availbleSMSCredit, toSendSMS, toSendEmail, toSendfacebook);
                billingManager.chargeSMSCreditUsed(latestEventVO, actualSMSNotify);
                String notification = "event found, notify ";
                if (toSendSMS) {
                    notification += " sms";
                }
                if (toSendEmail) {
                    notification += " email";
                }
                if (toSendfacebook) {
                    notification += " facebook";
                }
                myLog.debug(notification);
            } catch (CoreException ex) {
                myLog.error("Error", ex);
            }

        } else {
            myLog.debug("no event founds, nothing to notify");
        }
    }

    public List<SecqMeEventVO> getUserEventHistory(UserVO userVO, Integer startingRecord, Integer maxRecord) throws CoreException {
        myLog.debug("Getting user history event:" + userVO.getUserid() + ", start:" + startingRecord + ", max:" + maxRecord);
        List<SecqMeEventVO> eventVOList =
                getSecqMeEventDAO().getAllUserEvent(userVO.getUserid(), startingRecord, maxRecord);
//        for(SecqMeEventVO eventVO : eventVOList) {
//           // myLog.debug("Event->" + eventVO.getId() + ", " + eventVO.getMessage());
//        }

        return eventVOList;

    }

    public SecqMeEventVO findEventByEventID(Long eventID) throws CoreException {
        return getSecqMeEventDAO().read(eventID);
    }

    public void notifyTrialEvent(Long trialEventID, String mobileCountryISO, String mobileNumber, String langCode) throws CoreException {
        SecqMeEventVO eventVO = getSecqMeEventDAO().read(trialEventID);
        if (eventVO != null) {
            // Confirm safety for user Trial Event
            if (eventVO != null &&
                    EventStatusType.END != eventVO.getStatus() &&
                    EventStatusType.SAFE_NOTIFY != eventVO.getStatus()) {

                eventVO.setStatus(EventStatusType.END);
                eventVO.setConfirmSafetyTime(new Date());
                insertEventLogRecord(eventVO, EventStatusType.END, "Confirm Safety by User at:" + new Date(), null, null, null);

                getScheduleManager().removeExpireEventJob(eventVO);
                getSecqMeEventDAO().update(eventVO);
            }

            String finalLangCode = getLocaleManager().verifyLanguageCode(langCode);
            validateUserMobileNumber(mobileCountryISO, mobileNumber, finalLangCode);

            CountryVO countryVO = getSmsManager().getCountry(mobileCountryISO);
            getNotificationEngine().notifyTrialEvent(eventVO, DefaultUserManager.MARKET_DEFAULT, finalLangCode, countryVO, mobileNumber);
        }

    }

    public FullEventInfoVO getFullEventInfoOfContact(Long eventID) throws CoreException {
    	FullEventInfoVO fullEventInfoVO = null;
        myLog.debug("getFullEventInfoOfContact" + " eventID:" + eventID);
        SecqMeEventVO secqMeEventVO = getSecqMeEventDAO().read(eventID);

        if (secqMeEventVO != null) {
            fullEventInfoVO = new FullEventInfoVO();
            fullEventInfoVO.setSecqMeEventVO(secqMeEventVO);

            List<TrackingLogVO> trackingLogList = getTrackingLogDAO().findTrackingLogByEvendId(secqMeEventVO.getId(), Boolean.TRUE);
            fullEventInfoVO.setTrackingLogVOList(trackingLogList);

            List<EventLogVO> eventLogList = getEventLogDAO().findByEventId(secqMeEventVO.getId());
            fullEventInfoVO.setEventLogVOList(eventLogList);

            CrimeReportVO crimeReportVO = getCrimeReportDAO().findCrimeRerpotByEventID(secqMeEventVO.getId());
            fullEventInfoVO.setCrimeReportVO(crimeReportVO);

            if (secqMeEventVO.getEnablePicture()) {
                List<String> urls = getMediaFileManager().populateEventMediaUrl(secqMeEventVO, MediaFileType.PICTURE);
                if (!urls.isEmpty()) {
                    fullEventInfoVO.setEventPictureURL(urls.get(0));
                }
            }

            if (secqMeEventVO.getEnableVideo()) {
                fullEventInfoVO.setEmergencyVideoURLList(getMediaFileManager().populateEventMediaUrl(secqMeEventVO, MediaFileType.VIDEO));
            }

        } else {
            // if the event is not found or the event is not belong to the customer
            myLog.debug("secqMeEventVO error:" + secqMeEventVO);
            throw new CoreException(ErrorType.EVENT_NOT_FOUND_BY_EVENT_ID, eventID.toString());
        }
        return fullEventInfoVO;
    }
    
    public void shareEvent(UserVO userVO, Long eventId, List<Long> contactList) throws CoreException {
    	//
    	SecqMeEventVO secqMeEventVO = getSecqMeEventDAO().read(eventId);
    	if (secqMeEventVO != null) {
    		if (secqMeEventVO.getUserVO().getUserid().equalsIgnoreCase(userVO.getUserid())) {
    			secqMeEventVO.setEnableShare(true);
                updateShortUrlIfNeeded(secqMeEventVO);

                getSecqMeEventDAO().update(secqMeEventVO);
    			if (contactList == null) {
    				//send push to contact added by this user
        			if (userVO.getContactList() != null && userVO.getContactList().size() > 0) {
        				for (ContactVO contactVO : userVO.getContactList()) {
        					if (isAbleToReceivePushNotification(contactVO)) {
        						getNotificationEngine().sendEventPushNotification(userVO, contactVO.getContactUserVO(),
            							secqMeEventVO.getStatus().equals(EventStatusType.END) ? PushNotificationEventType.NORMAL_EVENT_END :
                                        PushNotificationEventType.NORMAL_EVENT, secqMeEventVO);
            					insertShareEventLog(userVO, secqMeEventVO, contactVO.getContactUserVO());
        					}
        				}

        				int availbleSMSCredit = billingManager.getAvailableSMSCredit(secqMeEventVO);
        		        int actualSMSSend = getNotificationEngine().sendShareEventEmailAndSms(secqMeEventVO, 
        		        		availbleSMSCredit, userVO.getContactList());
        		        billingManager.chargeSMSCreditUsed(secqMeEventVO, actualSMSSend);
        			}
    			} else {
    				//send push to contact chosen by the user
    				List<ContactVO> userContactList = new ArrayList<ContactVO>();
        			for(Long contactId : contactList) {
        				ContactVO contactVO = getContactDAO().read(contactId);
        				userContactList.add(contactVO);
        				if (isAbleToReceivePushNotification(contactVO)) {
        					getNotificationEngine().sendEventPushNotification(userVO, contactVO.getContactUserVO(),
                                    secqMeEventVO.getStatus().equals(EventStatusType.END) ? PushNotificationEventType.NORMAL_EVENT_END :
        							PushNotificationEventType.NORMAL_EVENT, secqMeEventVO);
            				insertShareEventLog(userVO, secqMeEventVO, contactVO.getContactUserVO());
        				}
        			}
        			int availbleSMSCredit = billingManager.getAvailableSMSCredit(secqMeEventVO);
    		        int actualSMSSend = getNotificationEngine().sendShareEventEmailAndSms(secqMeEventVO, 
    		        		availbleSMSCredit, userContactList);
    		        billingManager.chargeSMSCreditUsed(secqMeEventVO, actualSMSSend);
    			}
    		} else {
    			myLog.debug("shareEvent error1:" + secqMeEventVO);
        		throw new CoreException(ErrorType.EVENT_NOT_FOUND_BY_EVENT_ID, eventId.toString());
    		}
    	} else {
    		myLog.debug("shareEvent error2:" + secqMeEventVO);
    		throw new CoreException(ErrorType.EVENT_NOT_FOUND_BY_EVENT_ID, eventId.toString());
    	}
    }

    @Override
	public ShareEventVO getShareEventVO(String shareToUserid, Long eventid) {
		return getShareEventDAO().findByShareToUser(shareToUserid, eventid);
	}
    
    @Override 
    public List<ShareEventVO> getEventShareToUserWithinADay(UserVO userVO) {
    	Long oneDayBefore = System.currentTimeMillis() - (24 * 60 * MILLISECONDS_IN_A_MINUTE);
    	Date oneDayBeforeDate = new Date(oneDayBefore);
		return getShareEventDAO().findByShareTimeAndShareToUser(oneDayBeforeDate, userVO.getUserid());
	}
    
    private void insertShareEventLog(UserVO userVO, SecqMeEventVO secqMeEventVO, UserVO shareToUserVO) {
    	ShareEventVO shareEventVO = new ShareEventVO();
    	shareEventVO.setUserVO(userVO);
    	shareEventVO.setSecqMeEventVO(secqMeEventVO);
    	shareEventVO.setShareToUserVO(shareToUserVO);
    	shareEventVO.setShareTime(new Date());
    	getShareEventDAO().create(shareEventVO);
    }

}

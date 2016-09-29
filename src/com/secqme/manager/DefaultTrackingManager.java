package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.dao.TrackingLogDAO;
import com.secqme.domain.dao.UserTrackingLogDAO;
import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.TrackingLogVO;
import com.secqme.domain.model.UserTrackingLogVO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.manager.worker.TimeZoneWorker;
import com.secqme.util.TimeZoneUpdateService;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 *
 * @author jameskhoo
 */
public class DefaultTrackingManager extends BaseManager implements TrackingManager {

    private TrackingLogDAO trackingLogDAO = null;
    private UserTrackingLogDAO userTrackingLogDAO;
    private static final String ZERO_LOCATION_STR = "0.0, 0.0";
    private final ExecutorService threadPool;
    private static final Logger myLog = Logger.getLogger(DefaultTrackingManager.class);
    private EventManager eventManager = null;

    public DefaultTrackingManager(EventManager eventmgr,
            TrackingLogDAO trackingDAO,
            UserTrackingLogDAO userTrackingLogDAO,
            String mediaURLPath) {
        this.eventManager = eventmgr;
        this.trackingLogDAO = trackingDAO;
        this.userTrackingLogDAO = userTrackingLogDAO;
        threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public List<TrackingLogVO> getAllTrackingLog(long secQmeEventID, boolean filterZeroLocation) throws CoreException {
        List<TrackingLogVO> trackingLogList = trackingLogDAO.findTrackingLogByEvendId(secQmeEventID, filterZeroLocation);
        if (trackingLogList == null) {
            throw new CoreException(ErrorType.TRACKING_LOG_NOT_FOUND_ERROR, UserManager.USER_DEFAULT_LANGUAGE);
        }
        return trackingLogList;
    }

    @Override
    public void submitNewTrackingLog(TrackingLogVO trackingLogVO) throws CoreException {
        myLog.debug("Try to create new Tracking Log->" + trackingLogVO.getLocString());
        SecqMeEventVO eventVO = trackingLogVO.getSecqMeEventVO();
        if (trackingLogVO.getTimeReport() == null) {
            trackingLogVO.setTimeReport(new Date());
        }

        if (EventStatusType.NOTIFY.equals(eventVO.getStatus())) {
            trackingLogVO.setOtherInfo(TrackingLogVO.EMERGENCY_FLAG);
            if(eventVO.getEmergencyLatitude() == null) {
                eventVO.setEmergencyLatitude(trackingLogVO.getLatitude());
                eventVO.setEmergencyLongitude(trackingLogVO.getLongitude());
                eventVO.setEmergencyAccuracy(trackingLogVO.getAccuracy());
                getSecqMeEventDAO().update(eventVO);
            }
        }
        trackingLogDAO.create(trackingLogVO);
        myLog.debug("TrackingLog Created for" + trackingLogVO.getLatitude() + "," + trackingLogVO.getLongitude());
        
         if (eventVO.getEventTimeZone() == null) {
             myLog.debug("Submitting updating timezone worker to ThreadPool for" + eventVO.getUserVO().getUserid() +
                          " location:" +  trackingLogVO.getLatitude() + "," +  trackingLogVO.getLongitude());
             TimeZoneWorker tzWorker = new TimeZoneWorker((TimeZoneUpdateService) eventManager, eventVO, trackingLogVO.getLatitude(), trackingLogVO.getLongitude());
             FutureTask ft = new FutureTask(tzWorker);
             threadPool.submit(ft);
              myLog.debug("TimeZone Update Job done!" + eventVO.getUserVO().getUserid() + 
                          " location:" +  trackingLogVO.getLatitude() + "," +  trackingLogVO.getLongitude());
             //eventManager.updateEventTimeZone(eventVO, trackingLogVO.getLatitude(), trackingLogVO.getLongtitude());
        }
    }
    
    public void submitUserMovementLog(UserTrackingLogVO userTrackingLogVO) throws CoreException {
    	myLog.debug("Try to create new Movement Log->" + userTrackingLogVO.getUserid() + "," + userTrackingLogVO.getLatitude()
    			+ "," + userTrackingLogVO.getLongitude());
        userTrackingLogDAO.create(userTrackingLogVO);
    }

}

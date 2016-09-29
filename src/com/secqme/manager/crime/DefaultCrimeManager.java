package com.secqme.manager.crime;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.crime.CrimeType;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.manager.BaseManager;
import com.secqme.manager.worker.TimeZoneWorker;
import com.secqme.util.MediaFileType;
import com.secqme.util.TimeZoneUpdateService;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * User: James Khoo
 * Date: 2/18/14
 * Time: 6:15 PM
 */
public class DefaultCrimeManager extends BaseManager implements CrimeManager, TimeZoneUpdateService {
    private static Logger myLog = Logger.getLogger(DefaultCrimeManager.class);
    private static Integer DEFAULT_CITY_RADIUS_IN_KM = 20;
    private static Integer DEFAULT_CRIME_REPORT_HISTORY_IN_DAYS = 30;
    private final ExecutorService threadPool;

    /*
       Some CrimeScence API that we can use
       https://test.secq.me/mainportal/wom/track.jsf?trackingpin=2hNFBX
       https://test.secq.me/mainportal/wom/track.jsf?trackingpin=6X14k0
       https://test.secq.me/mainportal/wom/track.jsf?trackingpin=IMTifr

     */

    public DefaultCrimeManager() {
        threadPool = Executors.newCachedThreadPool();
    }

    public CrimeReportVO reportNewCrimeWithEvent(UserVO userVO, Long eventID, CrimeType crimeType,
                                                 String note, String encodedCrimePicStr) throws CoreException {
        myLog.debug("Creating new Crime Report for user->" + userVO.getUserid() + ",eventId:" + eventID);
        SecqMeEventVO eventVO = getSecqMeEventDAO().read(eventID);
        if (eventVO == null) {
            throw new CoreException(ErrorType.EVENT_NOT_FOUND_BY_EVENT_ID, userVO.getLangCode());
        }
        CrimeReportVO crimeReportVO = getCrimeReportDAO().findCrimeRerpotByEventID(eventID);
        if (crimeReportVO != null) {
            crimeReportVO.setNote(note);
            if (encodedCrimePicStr != null) {
                crimeReportVO.setCrimePictureURL(getMediaFileManager().saveCrimeReportMediaFile(crimeReportVO,
                        MediaFileType.PICTURE, encodedCrimePicStr));
            }
            getCrimeReportDAO().update(crimeReportVO);
        } else {
            crimeReportVO = submitNewCrimeReport(userVO, eventVO, crimeType, null, note,
                    encodedCrimePicStr, null, null, null);
        }
        return crimeReportVO;
    }

    public CrimeReportVO reportNewCrime(UserVO userVO, CrimeType crimeType, String timeRange,
                                        String note, String encodedCrimePicStr, Double latitude, Double longitude) throws CoreException {
        return submitNewCrimeReport(userVO, null, crimeType, timeRange, note, encodedCrimePicStr, latitude, longitude, null);
    }

    private CrimeReportVO submitNewCrimeReport(UserVO userVO, SecqMeEventVO eventVO, CrimeType crimeType, String timeRange, String note, String encodedCrimePicStr,
                                               Double latitude, Double longitude, Double accuracy) throws CoreException {
        CrimeReportVO crimeReportVO = new CrimeReportVO();
        crimeReportVO.setUserVO(userVO);
        crimeReportVO.setTimeRange(timeRange);
        if (eventVO != null) {
            crimeReportVO.setEventId(eventVO.getId());
            crimeReportVO.setReportDate(eventVO.getEmergencyTriggerAt() == null ? new Date() : eventVO.getEmergencyTriggerAt());
            crimeReportVO.setTimeZone(eventVO.getEventTimeZone());
            if (eventVO.getEmergencyLatitude() != null) {
                crimeReportVO.setLatitude(eventVO.getEmergencyLatitude());
                crimeReportVO.setLongitude(eventVO.getEmergencyLongitude());
                crimeReportVO.setAccuracy(eventVO.getEmergencyAccuracy());
            }
        } else {
            crimeReportVO.setReportDate(new Date());
            crimeReportVO.setLatitude(latitude);
            crimeReportVO.setLongitude(longitude);
            crimeReportVO.setAccuracy(accuracy);
        }
        crimeReportVO.setCrimeType(crimeType);
        crimeReportVO.setNote(note);
        getCrimeReportDAO().create(crimeReportVO);

        if (encodedCrimePicStr != null) {
            crimeReportVO.setCrimePictureURL(getMediaFileManager().saveCrimeReportMediaFile(crimeReportVO,
                    MediaFileType.PICTURE, encodedCrimePicStr));
            getCrimeReportDAO().update(crimeReportVO);
        }

        // Todo, find out when is the emergency is trigger, get the tracking Log.. and populate the Location
        if (crimeReportVO.getTimeZone() == null) {
            TimeZoneWorker tzWorker = new TimeZoneWorker(this, crimeReportVO, crimeReportVO.getLatitude(), crimeReportVO.getLongitude());
            FutureTask ft = new FutureTask(tzWorker);
            threadPool.submit(ft);
        }
        return crimeReportVO;
    }

    @Override
    public void updateTimeZone(Object valueObject, Double latitude, Double longitude) throws CoreException {
        CrimeReportVO crimeReportVO = (CrimeReportVO) valueObject;
        myLog.debug("Updating timeZone for CrimeReport->" + crimeReportVO.getId() + ", type->" + crimeReportVO.getCrimeType());
        crimeReportVO.setTimeZone(getLocationUtil().getTimeZone(latitude, longitude));
        getCrimeReportDAO().update(crimeReportVO);
    }


    public List<CrimeReportVO> findCrimeReportNearMe(Double latitude, Double longitude,
                                                   Integer distanceInKM, Integer dayPass) throws CoreException {
        if(distanceInKM == null) {
            distanceInKM = DEFAULT_CITY_RADIUS_IN_KM;
        }

        if(dayPass == null) {
            dayPass = DEFAULT_CRIME_REPORT_HISTORY_IN_DAYS;
        }

        return getCrimeReportDAO().findCrimeReport(latitude, longitude, distanceInKM, dayPass);
    }
}

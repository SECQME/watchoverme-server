package com.secqme.domain.dao;

import com.secqme.domain.model.event.SecqMeEventVO;
import java.util.Date;
import java.util.List;

/**
 *
 * @author james
 */
public interface SecqMeEventDAO extends BaseDAO<SecqMeEventVO, Long> {
    public List<SecqMeEventVO> findNewEventsBetweenEndTime(Date startTime, Date endTime);
    public List<SecqMeEventVO> findConfirmEventsBetweenEndTime(Date startTime, Date endTime);
    public List<SecqMeEventVO> findNewEventsLessThenEndTime(Date endTime);
    public List<SecqMeEventVO> findConfirmEventsLessThenEndTime(Date endTime);
    public List<SecqMeEventVO> findNewConfirmEventByUser(String userid);
    public List<SecqMeEventVO> getAllUserEvent(String userid, Integer startingRecord, Integer maxRecord);
    public List<SecqMeEventVO> findUserEventBetweenDates(String userid, Date startTime, Date endTime);
    public SecqMeEventVO findEventByTrackingPin(String trackingPin);
    public SecqMeEventVO getUserLatestEvent(String userid);
    public Long getNumberOfEventRegistered(String userid);
}

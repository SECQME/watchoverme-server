package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SecqMeEventDAO;
import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.event.SecqMeEventVO;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * @author james
 */
public class SecqMeEventJPADAO extends BaseJPADAO<SecqMeEventVO, Long> implements SecqMeEventDAO {

    public SecqMeEventJPADAO() {
        super(SecqMeEventVO.class);
    }

    public List<SecqMeEventVO> findNewEventsBetweenEndTime(Date startTime, Date endTime) {
        return findEventsBetweenEndTime(startTime, endTime, EventStatusType.NEW);

    }

    public List<SecqMeEventVO> findConfirmEventsBetweenEndTime(Date startTime, Date endTime) {
        return findEventsBetweenEndTime(startTime, endTime, EventStatusType.CONFIRM);
    }

    private List<SecqMeEventVO> findEventsBetweenEndTime(Date startTime, Date endTime, EventStatusType statusType) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .setParameter("status", statusType);
        return executeQueryWithResultList(SecqMeEventVO.QUERY_FIND_EVENT_ENDTIME_INBETWEEN, parameter);
    }

    public List<SecqMeEventVO> findNewEventsLessThenEndTime(Date endTime) {
        return findEventLessThenEndTime(endTime, EventStatusType.NEW);

    }

    public List<SecqMeEventVO> findConfirmEventsLessThenEndTime(Date endTime) {
        return findEventLessThenEndTime(endTime, EventStatusType.CONFIRM);
    }

    private List<SecqMeEventVO> findEventLessThenEndTime(Date endTime, EventStatusType statusType) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("endTime", endTime)
                .setParameter("status", statusType);
        return executeQueryWithResultList(SecqMeEventVO.QUERY_FIND_EVENT_LESS_THEN_ENDTIME, parameter);
    }

    public List<SecqMeEventVO> findNewConfirmEventByUser(String userid) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("userid", userid)
                .setParameter("newStatus", EventStatusType.NEW)
                .setParameter("confirmStatus", EventStatusType.CONFIRM);
        return executeQueryWithResultList(SecqMeEventVO.QUERY_FIND_NEW_CONFIRM_EVENT_BY_USER, parameter);
    }

    public List<SecqMeEventVO> getAllUserEvent(String userid, Integer startingRecord, Integer maxRecord) {

        List<SecqMeEventVO> secqMeEventVOList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery(SecqMeEventVO.QUERY_GET_ALL_USER_EVENTS);
            query = query.setParameter("userid", userid);
            if(startingRecord != null) {
                query.setFirstResult(startingRecord);
            }
            if(maxRecord != null) {
                query.setMaxResults(maxRecord);
            }
            secqMeEventVOList = (List<SecqMeEventVO>) query.getResultList();
        } catch (NoResultException nre) {
            myLog.debug(String.format("No user event offser %d limit %d.", startingRecord, maxRecord));
        } finally {
            em.close();
        }
        return secqMeEventVOList;

    }

    public SecqMeEventVO getUserLatestEvent(String userid) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("userid", userid);
        return executeQueryWithSingleResult(SecqMeEventVO.QUERY_FIND_USER_LATEST_EVENT, parameter);
    }

    public SecqMeEventVO findEventByTrackingPin(String trackingPin) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("trackingPin", trackingPin);
        return executeQueryWithSingleResult(SecqMeEventVO.QUERY_FIND_BY_TRACKING_PIN, parameter);
    }

    public List<SecqMeEventVO> findUserEventBetweenDates(String userid, Date startTime, Date endTime) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("userid", userid)
                .setParameter("endTime", endTime)
                .setParameter("startTime", startTime);
        return executeQueryWithResultList(SecqMeEventVO.QUERY_FIND_USER_EVENT_BETWEEN_DATES, parameter);
    }

    public Long getNumberOfEventRegistered(String userid) {
        Long countResult = 0l;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery(SecqMeEventVO.QUERY_COUNT_EVENT);
            query.setParameter("userid", userid);
            countResult = (Long) query.getSingleResult();
        } finally {
            em.close();
        }
        return countResult;
    }

}

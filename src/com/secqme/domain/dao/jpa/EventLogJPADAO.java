package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.EventLogDAO;
import com.secqme.domain.model.event.EventLogVO;
import com.secqme.util.MediaFileType;

import java.util.List;

/**
 *
 * @author james
 */
public class EventLogJPADAO extends BaseJPADAO<EventLogVO, Long> implements EventLogDAO {

    public EventLogJPADAO() {
        super(EventLogVO.class);
    }

    public List<EventLogVO> findByEventId(Long eventId) {
        JPAParameter parameter  =  new JPAParameter()
                .setParameter("eventId", eventId);
        return executeQueryWithResultList(EventLogVO.QUERY_FIND_BY_EVENTID, parameter);
    }


    public List<EventLogVO> findByEventIdAndMediaType(Long eventId, MediaFileType mediaFileType) {
        JPAParameter parameter  =  new JPAParameter()
                .setParameter("eventId", eventId)
                .setParameter("mediaType", mediaFileType);
        return executeQueryWithResultList(EventLogVO.QUERY_FIND_BY_EVENTID_AND_MEDIA_TYPE, parameter);
    }

}
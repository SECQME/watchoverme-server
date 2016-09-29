package com.secqme.domain.dao;

import com.secqme.domain.model.event.EventLogVO;
import com.secqme.util.MediaFileType;

import java.util.List;

/**
 *
 * @author james
 */
public interface EventLogDAO extends BaseDAO<EventLogVO, Long> {

    public List<EventLogVO> findByEventId(Long eventId);
    public List<EventLogVO> findByEventIdAndMediaType(Long eventId, MediaFileType mediaFileType);
}

package com.secqme.domain.dao;

import com.secqme.domain.model.TrackingLogVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface TrackingLogDAO extends BaseDAO<TrackingLogVO, Long> {
    public List<TrackingLogVO> findTrackingLogByEvendId(Long eventID, Boolean filterZeroLocation);
}

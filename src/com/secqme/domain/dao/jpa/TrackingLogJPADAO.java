package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.TrackingLogDAO;
import com.secqme.domain.model.TrackingLogVO;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author james
 */
public class TrackingLogJPADAO extends BaseJPADAO<TrackingLogVO, Long> implements TrackingLogDAO {
    private static final String ZERO_LOCATION_STR = "0.0, 0.0";

    public TrackingLogJPADAO() {
        super(TrackingLogVO.class);
    }

    public List<TrackingLogVO> findTrackingLogByEvendId(Long eventID, Boolean filterZeroLocation) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("secqMeid", eventID);

        List<TrackingLogVO> trackingLogVOList = executeQueryWithResultList(TrackingLogVO.QUERY_FIND_BY_SECQME_ID, parameter);
        List<TrackingLogVO> returnTrackingLogList;

        if (filterZeroLocation) {
            returnTrackingLogList = new ArrayList<TrackingLogVO>();
            for (TrackingLogVO trackingLogVO : trackingLogVOList) {
                if (!ZERO_LOCATION_STR.equals(trackingLogVO.getLocString())) {
                    returnTrackingLogList.add(trackingLogVO);
                }
            }
        } else {
            returnTrackingLogList = trackingLogVOList;
        }

        return returnTrackingLogList;
    }


}

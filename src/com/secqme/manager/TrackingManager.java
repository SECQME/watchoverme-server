package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.domain.model.TrackingLogVO;
import com.secqme.domain.model.UserTrackingLogVO;

import java.util.List;

/**
 *
 * @author jameskhoo
 */
public interface TrackingManager {

    public List<TrackingLogVO> getAllTrackingLog(long secQmeEventID, boolean filterZeroLocation) throws CoreException;
    public void submitNewTrackingLog(TrackingLogVO trackingLogVO) throws CoreException;
    public void submitUserMovementLog(UserTrackingLogVO userTrackingLogVO) throws CoreException;
}

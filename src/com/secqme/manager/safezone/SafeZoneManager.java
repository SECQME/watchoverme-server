package com.secqme.manager.safezone;

import java.util.List;

import com.secqme.CoreException;
import com.secqme.domain.model.safezone.SafeZoneVO;
import com.secqme.domain.model.UserVO;

/**
 * User: James Khoo
 * Date: 2/10/14
 * Time: 11:25 AM
 */
public interface SafeZoneManager {
    public UserVO addNewSafeZone(UserVO userVO, SafeZoneVO safeZoneVO) throws CoreException;
    public UserVO deleteSafeZone(UserVO userVO, Long safeZoneID) throws CoreException;
    public UserVO updateSafeZone(UserVO userVO, Long safeZoneID, String newSafeZoneName, String newSafeZoneAddress,
                               Double newLatitude, Double newLongtitude, Double newAccuracy,
                               Boolean autoConfirmSafety) throws CoreException;
    public UserVO deleteMultipleSafeZone(UserVO userVO, List<Long> safeZoneIDList) throws CoreException;
}

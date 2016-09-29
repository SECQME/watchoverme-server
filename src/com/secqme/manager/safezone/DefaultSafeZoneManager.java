package com.secqme.manager.safezone;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.safezone.SafeZoneVO;
import com.secqme.manager.BaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * User: James Khoo
 * Date: 2/10/14
 * Time: 11:28 AM
 */
public class DefaultSafeZoneManager extends BaseManager implements SafeZoneManager {

    public DefaultSafeZoneManager() {

    }

    public UserVO addNewSafeZone(UserVO userVO, SafeZoneVO safeZoneVO) throws CoreException {
        if (userVO.getUserCreatedSafeZoneList() == null) {
            userVO.setUserCreatedSafeZoneList(new ArrayList<SafeZoneVO>());
        }

        // Check if there is a duplicate SafeZone (i.e SafeZone with same name)
        Boolean duplicateFound = false;
        for(SafeZoneVO userSafeZone : userVO.getUserCreatedSafeZoneList()) {
             if(userSafeZone.getZoneInternalName().equalsIgnoreCase(safeZoneVO.getZoneInternalName())) {
                 duplicateFound = true;
             }
        }
        if (!duplicateFound) {
            userVO.getUserCreatedSafeZoneList().add(safeZoneVO);
            getUserDAO().update(userVO);
        } else {
            throw new CoreException(ErrorType.DUPLICATIE_SAFEZONE_NAME_ERROR, userVO.getLangCode());
        }
        return userVO;
    }

    public UserVO deleteSafeZone(UserVO userVO, Long safeZoneID) throws CoreException {
        if(userVO.getUserCreatedSafeZoneList() != null) {
           Boolean safeZoneFound = false;
           for(SafeZoneVO userSafeZone : userVO.getUserCreatedSafeZoneList()) {
               if(userSafeZone.getId().equals(safeZoneID)) {
                   userVO.getUserCreatedSafeZoneList().remove(userSafeZone);
                   safeZoneFound = true;
                   break;
               }
           }

           if(safeZoneFound) {
               getUserDAO().update(userVO);
           }
        }
        return userVO;
    }


    public UserVO updateSafeZone(UserVO userVO, Long safeZoneID, String newSafeZoneName, String newSafeZoneAddress,
                               Double newLatitude, Double newLongitude, Double newAccuracy,
                               Boolean autoConfirmSafety) throws CoreException {

        if(userVO.getUserCreatedSafeZoneList() != null) {
            for(SafeZoneVO userSafeZone : userVO.getUserCreatedSafeZoneList()) {
                if(userSafeZone.getId().equals(safeZoneID)) {
                    if(newSafeZoneName != null) {
                        userSafeZone.setZoneName(newSafeZoneName);
                    }

                    if(newSafeZoneAddress != null) {
                        userSafeZone.setAddress(newSafeZoneAddress);
                    }

                    if(newLatitude != null && newLongitude !=null && newAccuracy !=null) {
                        userSafeZone.setLatitude(newLatitude);
                        userSafeZone.setLongitude(newLongitude);
                        userSafeZone.setRadius(newAccuracy);
                    }

                    if(autoConfirmSafety != null) {
                        userSafeZone.setAutoConfirmSafety(autoConfirmSafety);
                    }
                    getUserDAO().update(userVO);
                    break;
                }
            }
        }

        return userVO;
    }
    
    public UserVO deleteMultipleSafeZone(UserVO userVO, List<Long> safeZoneIDList) throws CoreException {
    	if(userVO.getUserCreatedSafeZoneList() != null) {
            Boolean safeZoneFound = false;
            for(Long safeZoneId : safeZoneIDList) {
            	for(SafeZoneVO userSafeZone : userVO.getUserCreatedSafeZoneList()) {
                    if(userSafeZone.getId().equals(safeZoneId)) {
                        userVO.getUserCreatedSafeZoneList().remove(userSafeZone);
                        safeZoneFound = true;
                        break;
                    }
                }

                if(safeZoneFound) {
                    getUserDAO().update(userVO);
                }
            }
            
         }
         return userVO;
    }
}

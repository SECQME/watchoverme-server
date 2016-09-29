package com.secqme.manager.crime;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.crime.CrimeType;

import java.util.List;

/**
 * User: James Khoo
 * Date: 2/18/14
 * Time: 6:14 PM
 */
public interface CrimeManager {

    public CrimeReportVO reportNewCrimeWithEvent(UserVO userVO, Long eventID, CrimeType crimeType, String note, String encodedCrimePic) throws CoreException;
    public CrimeReportVO reportNewCrime(UserVO userVO, CrimeType crimeType, String timeRange,
                                        String note, String encodedCrimePicStr, Double latitude, Double longitude)
            throws CoreException;
    public List<CrimeReportVO> findCrimeReportNearMe(Double latitude, Double longitude,
                                                   Integer distanceInKM, Integer dayPass) throws CoreException;

}

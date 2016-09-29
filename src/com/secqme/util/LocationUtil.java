package com.secqme.util;

import com.secqme.CoreException;
import com.secqme.domain.model.location.GooglePlaceCacheVO;

/**
 *
 * @author coolboykl
 */
public interface LocationUtil {
    // return the TimeZone as String acceptable Java SimpleDateFormat
    public String getTimeZone(Double latitude, Double longtitude) throws CoreException;
    public String getCountryCodeByLocation(Double latitude, Double longtitude) throws CoreException;
    public GooglePlaceCacheVO getGooglePlaceResult(String placeName) throws CoreException;
    public double distanceBetweenTwoLocationInKM(double lat1, double lon1, double lat2, double lon2);
    public String getStreetAddr(Double latitude, Double longitude);
}

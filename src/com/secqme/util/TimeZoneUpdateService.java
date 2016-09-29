package com.secqme.util;

import com.secqme.CoreException;

/**
 * User: James Khoo
 * Date: 3/12/14
 * Time: 5:18 PM
 */
public interface TimeZoneUpdateService {

    public void updateTimeZone(Object valueObject, Double latitude, Double longitude) throws CoreException;
}

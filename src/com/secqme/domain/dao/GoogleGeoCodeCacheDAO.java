package com.secqme.domain.dao;

import com.secqme.domain.model.location.GoogleGeoCodeCacheVO;

/**
 * User: James Khoo
 * Date: 2/22/14
 * Time: 12:14 PM
 */
public interface GoogleGeoCodeCacheDAO extends BaseDAO<GoogleGeoCodeCacheVO, String> {
    public GoogleGeoCodeCacheVO findGoogleGeoCodeByLatLong(Double latitude, Double longitude);
}

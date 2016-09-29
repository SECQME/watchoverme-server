package com.secqme.domain.dao;

import com.secqme.domain.model.location.GooglePlaceCacheVO;

/**
 * User: James Khoo
 * Date: 2/20/14
 * Time: 1:19 PM
 */
public interface GooglePlaceCacheDAO extends BaseDAO<GooglePlaceCacheVO, String> {
    public GooglePlaceCacheVO findGooglePlaceByName(String placeName);
}

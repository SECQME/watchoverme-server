package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.GooglePlaceCacheDAO;
import com.secqme.domain.model.location.GooglePlaceCacheVO;

/**
 * User: James Khoo
 * Date: 2/20/14
 * Time: 1:21 PM
 */
public class GooglePlaceCahceJPADAO extends BaseJPADAO<GooglePlaceCacheVO, String> implements GooglePlaceCacheDAO {
    public GooglePlaceCahceJPADAO() {
        super(GooglePlaceCacheVO.class);
    }

    public GooglePlaceCacheVO findGooglePlaceByName(String placeName) {
        JPAParameter parameter = new JPAParameter().setParameter("placeName", placeName);
        return executeQueryWithSingleResult(GooglePlaceCacheVO.QUERY_FIND_BY_NAME, parameter);
    }
}

package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.GoogleGeoCodeCacheDAO;
import com.secqme.domain.model.location.GoogleGeoCodeCacheVO;

import java.text.DecimalFormat;

/**
 * User: James Khoo
 * Date: 2/22/14
 * Time: 12:15 PM
 */
public class GoogleGeoCodeCacheJAPDAO extends BaseJPADAO<GoogleGeoCodeCacheVO, String> implements GoogleGeoCodeCacheDAO {

    private DecimalFormat locationDecimalFormat = null;


    public GoogleGeoCodeCacheJAPDAO() {
        super(GoogleGeoCodeCacheVO.class);
        locationDecimalFormat = new DecimalFormat("#.00");
    }

    public GoogleGeoCodeCacheVO findGoogleGeoCodeByLatLong(Double latitude, Double longitude) {
        String locationKey = locationDecimalFormat.format(latitude) + "," + locationDecimalFormat.format(longitude);
        return this.read(locationKey);

    }
}

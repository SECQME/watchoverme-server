package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.TimeZoneDAO;
import com.secqme.domain.model.TimeZoneVO;

/**
 *
 * @author james
 */
public class TimeZoneJPADAO extends BaseJPADAO<TimeZoneVO, String> implements TimeZoneDAO {

    public TimeZoneJPADAO() {
        super(TimeZoneVO.class);
    }

}
package com.secqme.domain.dao;

import com.secqme.domain.model.crime.CrimeReportVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 2/18/14
 * Time: 5:58 PM
 */
public interface CrimeReportDAO extends BaseDAO<CrimeReportVO, Long> {
    public List<CrimeReportVO> findCrimeReportByUser(String userid);
    public CrimeReportVO findCrimeRerpotByEventID(Long eventID);
    public List<CrimeReportVO> findCrimeReport(Double latitude, Double longitude, Integer distanceInKM, Integer dayPass);
}

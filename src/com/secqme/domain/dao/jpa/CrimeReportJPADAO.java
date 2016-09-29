package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.CrimeReportDAO;
import com.secqme.domain.model.crime.CrimeReportVO;
import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: James Khoo
 * Date: 2/18/14
 * Time: 6:01 PM
 */
public class CrimeReportJPADAO extends BaseJPADAO<CrimeReportVO, Long> implements CrimeReportDAO {
    private static final String SQL_FIND_CRIME_REPORT_BY_DISTANCE =
            "SELECT id, userid, reportDate, timeRange, timeZone, eventId, crimeType, note, latitude, longitude, accuracy, crimePictureURL, " +
                    " (6371 * acos( cos( radians(MY_LATITUDE) ) " +
                    " * cos( radians( latitude) ) * cos( radians(MY_LONGITUDE) - " +
                    " radians(longitude) ) + sin( radians(MY_LATITUDE) ) * sin( radians(latitude) ) )) AS distance " +
                    "FROM crimeReport " +
                    "WHERE reportDate >= \'START_DATE\' AND reportDate <= \'END_DATE\' " +
                    "HAVING distance < DISTANCE_IN_KM " +
                    "ORDER BY distance";

    private static final String FIND_CRIME_REPORT_WITH_ID_LIST_QUERY =
            "SELECT o from CrimeReportVO o WHERE o.id IN :idList";

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");


    public CrimeReportJPADAO() {
        super(CrimeReportVO.class);
    }

    public List<CrimeReportVO> findCrimeReportByUser(String userid) {
        JPAParameter parameter = new JPAParameter().setParameter("userid", userid);

        return executeQueryWithResultList(CrimeReportVO.QUERY_FIND_BY_USER_ID, parameter);
    }


    public CrimeReportVO findCrimeRerpotByEventID(Long eventID) {
        JPAParameter parameter = new JPAParameter().setParameter("eventId", eventID);

        return executeQueryWithSingleResult(CrimeReportVO.QUERY_FIND_BY_EVENT_ID, parameter);
    }

    public List<CrimeReportVO> findCrimeReport(Double latitude, Double longitude, Integer distanceInKM, Integer dayPass){
        List<CrimeReportVO> crimeReportVOList = null;
        List<Long> crimeIDList = null;
        Date toDayDate = new Date();
        Date startDate = DateUtils.addDays(toDayDate, -dayPass);
        Date endDate = DateUtils.addDays(toDayDate, 1);
        String endDateStr = dateTimeFormat.format(endDate);
        String startDateStr = dateTimeFormat.format(startDate);
        try {
            String finalSQL = SQL_FIND_CRIME_REPORT_BY_DISTANCE.replace("MY_LATITUDE", latitude.toString())
                    .replace("MY_LONGITUDE", longitude.toString())
                    .replace("DISTANCE_IN_KM", distanceInKM.toString())
                    .replace("START_DATE", startDateStr)
                    .replace("END_DATE", endDateStr);
//              myLog.debug("Final SQL to Execute->" + finalSQL);
            JSONArray resultArray = executeStatement(finalSQL);
            if (resultArray != null && resultArray.length() > 0) {
                crimeIDList = new ArrayList<Long>();
                for(int i = 0; i < resultArray.length(); i++) {
                    JSONObject resultObj = resultArray.getJSONObject(i);
                    crimeIDList.add(resultObj.getLong("id"));
                }
                EntityManager em = emf.createEntityManager();
                em.getTransaction().begin();
                Query theQuery = em.createQuery(FIND_CRIME_REPORT_WITH_ID_LIST_QUERY);
                theQuery.setParameter("idList", crimeIDList);
                crimeReportVOList = theQuery.getResultList();
                em.getTransaction().commit();
            }
        } catch(JSONException ex) {
            myLog.error("Failed to format JSON", ex);
        }

       return crimeReportVOList;
    }
}

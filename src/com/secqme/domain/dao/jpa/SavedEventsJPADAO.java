package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SavedEventsDAO;
import com.secqme.domain.model.crime.CrimeReportVO;
import com.secqme.domain.model.event.SavedEventVO;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

public class SavedEventsJPADAO extends BaseJPADAO<SavedEventVO, Long> implements SavedEventsDAO {

    private static Logger myLog = Logger.getLogger(SecqMeEventJPADAO.class);

    public SavedEventsJPADAO() {
        super(SavedEventVO.class);
    }

    public List<SavedEventVO> findEventsByUserid(String userid) {
        JPAParameter parameter = new JPAParameter().setParameter("userid", userid);
        return executeQueryWithResultList(SavedEventVO.QUERY_FIND_EVENTS_BY_USERID, parameter);
    }
}
package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ARMarketMessageTemplateDAO;
import com.secqme.domain.model.ar.ARMarketMessageTemplateVO;
import org.apache.log4j.Logger;

import java.util.List;

/**
 *
 * @author james
 */
public class ARMarketMessageTemplateJPADAO extends BaseJPADAO<ARMarketMessageTemplateVO, String> implements ARMarketMessageTemplateDAO {

    private static final Logger myLog = Logger.getLogger(ARMarketMessageTemplateJPADAO.class);

    public ARMarketMessageTemplateJPADAO() {
        super(ARMarketMessageTemplateVO.class);
    }

    public List<ARMarketMessageTemplateVO> findAll() {
        return executeQueryWithResultList(ARMarketMessageTemplateVO.QUERY_FIND_ALL);
    }
}

package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.MarketDAO;
import com.secqme.domain.model.billing.MarketVO;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author james
 */
public class MarketJPADAO extends BaseJPADAO<MarketVO, String> implements MarketDAO, Serializable {

    public MarketJPADAO() {
        super(MarketVO.class);
    }

    public List<MarketVO> findALL() {
        return executeQueryWithResultList(MarketVO.QUERY_FIND_ALL);
    }

}

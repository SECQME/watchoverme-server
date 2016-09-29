package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.MarketingTextConfigDAO;
import com.secqme.domain.model.util.MarketingTextConfigVO;
import com.secqme.domain.model.util.MarketingTextConfigVOPK;

import java.util.List;

/**
 * User: James Khoo
 * Date: 3/28/14
 * Time: 4:58 PM
 */
public class MarketingTextConfigJPADAO extends BaseJPADAO<MarketingTextConfigVO, MarketingTextConfigVOPK> implements MarketingTextConfigDAO {

    public MarketingTextConfigJPADAO() {
        super(MarketingTextConfigVO.class);
    }

    @Override
    public List<MarketingTextConfigVO> findAll() {
        return executeQueryWithResultList(MarketingTextConfigVO.QUERY_FIND_ALL);
    }
}

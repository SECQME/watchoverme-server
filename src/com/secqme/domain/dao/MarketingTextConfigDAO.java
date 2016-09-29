package com.secqme.domain.dao;

import com.secqme.domain.model.util.MarketingTextConfigVO;
import com.secqme.domain.model.util.MarketingTextConfigVOPK;

import java.util.List;

/**
 * User: James Khoo
 * Date: 3/28/14
 * Time: 4:57 PM
 */
public interface MarketingTextConfigDAO extends BaseDAO<MarketingTextConfigVO, MarketingTextConfigVOPK> {
    public List<MarketingTextConfigVO> findAll();
}

package com.secqme.domain.dao;

import com.secqme.domain.model.billing.MarketVO;
import com.secqme.domain.model.UserVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface MarketDAO extends BaseDAO<MarketVO, String> {
    public List<MarketVO> findALL();
}

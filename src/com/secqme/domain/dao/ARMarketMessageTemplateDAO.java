package com.secqme.domain.dao;

import com.secqme.domain.model.ar.ARMarketMessageTemplateVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface ARMarketMessageTemplateDAO extends BaseDAO<ARMarketMessageTemplateVO, String> {
    public List<ARMarketMessageTemplateVO> findAll();
}

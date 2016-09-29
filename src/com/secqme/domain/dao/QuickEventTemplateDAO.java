package com.secqme.domain.dao;

import com.secqme.domain.model.event.QuickEventTemplateVO;
import com.secqme.domain.model.event.QuickEventTemplateVOPK;

import java.util.List;

/**
 * User: James Khoo
 * Date: 11/18/13
 * Time: 11:30 AM
 */
public interface QuickEventTemplateDAO extends BaseDAO<QuickEventTemplateVO, QuickEventTemplateVOPK> {
    public List<QuickEventTemplateVO> findAll();

}

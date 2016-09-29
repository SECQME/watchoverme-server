package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.QuickEventTemplateDAO;
import com.secqme.domain.model.event.QuickEventTemplateVO;
import com.secqme.domain.model.event.QuickEventTemplateVOPK;

import java.util.List;

/**
 * User: James Khoo
 * Date: 11/18/13
 * Time: 11:31 AM
 */
public class QuickEventTemplateJPADAO extends BaseJPADAO<QuickEventTemplateVO, QuickEventTemplateVOPK> implements QuickEventTemplateDAO {

    public QuickEventTemplateJPADAO(){
        super(QuickEventTemplateVO.class);
    }

    @Override
    public List<QuickEventTemplateVO> findAll() {
        return executeQueryWithResultList(QuickEventTemplateVO.QUERY_FIND_ALL);
    }

}

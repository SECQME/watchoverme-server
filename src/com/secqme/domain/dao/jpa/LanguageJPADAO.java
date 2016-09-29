package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.LanguageDAO;
import com.secqme.domain.model.LanguageVO;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author james
 */
public class LanguageJPADAO extends BaseJPADAO<LanguageVO, String> implements LanguageDAO, Serializable {

    public LanguageJPADAO() {
        super(LanguageVO.class);
    }

    public List<LanguageVO> findAll() {
        return executeQueryWithResultList(LanguageVO.QUERY_FIND_ALL);
    }

    @Override
    public LanguageVO findDefaultLanguage() {
        return executeQueryWithSingleResult(LanguageVO.QUERY_FIND_DEFAULT_LANGUAGE);
    }
}

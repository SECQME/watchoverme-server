package com.secqme.domain.dao;

import com.secqme.domain.model.LanguageVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface LanguageDAO extends BaseDAO<LanguageVO, String> {
    public List<LanguageVO> findAll();
    public LanguageVO findDefaultLanguage();
}

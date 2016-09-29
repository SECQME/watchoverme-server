package com.secqme.util.ar;

import com.secqme.domain.dao.ARMarketMessageTemplateDAO;
import com.secqme.domain.dao.LanguageDAO;
import com.secqme.domain.model.ar.ARMessageTemplateVO;

/**
 * Created by edward on 15/05/2015.
 */
public class MandrillARTemplateEngine extends BaseARTemplateEngine {

    public MandrillARTemplateEngine(ARMarketMessageTemplateDAO arMarketMessageTemplateDAO, LanguageDAO languageDAO) {
        super(arMarketMessageTemplateDAO, languageDAO);
        refresh();
    }

    @Override
    public String getEngineName() {
        return ARMessageTemplateVO.ENGINE_MANDRILL;
    }
}

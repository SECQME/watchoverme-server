package com.secqme.util.ar;

import com.secqme.domain.dao.ARMarketMessageTemplateDAO;
import com.secqme.domain.dao.LanguageDAO;
import com.secqme.domain.model.LanguageVO;
import com.secqme.domain.model.ar.ARMarketMessageTemplateVO;
import com.secqme.domain.model.ar.ARMessageTemplateVO;
import com.secqme.domain.model.ar.ARMessageTemplateField;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 14/05/2015.
 */
public abstract class BaseARTemplateEngine implements ARTemplateEngine {
    private static Logger myLog = Logger.getLogger(BaseARTemplateEngine.class);

    protected LanguageDAO languageDAO;
    protected ARMarketMessageTemplateDAO arMarketMessageTemplateDAO = null;

    protected Map<String, ARMessageTemplateVO> messageTemplates;
    protected List<ARMarketMessageTemplateVO> arMarketTemplateList;
    protected String defaultLangCode;

    public BaseARTemplateEngine(ARMarketMessageTemplateDAO arMarketMessageTemplateDAO, LanguageDAO languageDAO) {
        this.arMarketMessageTemplateDAO = arMarketMessageTemplateDAO;
        this.languageDAO = languageDAO;
    }

    @Override
    public void refresh() {
        myLog.debug("Refreshing all templates...");

        LanguageVO defaultLanguageVO = languageDAO.findDefaultLanguage();
        defaultLangCode = defaultLanguageVO.getCode();
        messageTemplates = new HashMap<String, ARMessageTemplateVO>();

        arMarketTemplateList = arMarketMessageTemplateDAO.findAll();
        for (ARMarketMessageTemplateVO arMarketMsgTemplateVO : arMarketTemplateList) {
            if (arMarketMsgTemplateVO.getMessageTemplateList() != null && !arMarketMsgTemplateVO.getMessageTemplateList().isEmpty()) {
                for (ARMessageTemplateVO templateVO : arMarketMsgTemplateVO.getMessageTemplateList()) {
                    String templateKey = generateTemplateKey(arMarketMsgTemplateVO.getMarketVO().getName(), templateVO.getTemplateCode(), templateVO.getLangCode());
                    messageTemplates.put(templateKey, templateVO);
                }
            }
        }

        myLog.debug(String.format("Found %d templates.", messageTemplates.size()));
    }

    @Override
    public List<ARMarketMessageTemplateVO> getMarketTemplates(String marketCode) {
        List<ARMarketMessageTemplateVO> marketTemplateList = new ArrayList<ARMarketMessageTemplateVO>();

        for (ARMarketMessageTemplateVO marketTemplateVO : arMarketTemplateList) {
            if (marketTemplateVO.getMarketVO().getName().endsWith(marketCode)) {
                marketTemplateList.add(marketTemplateVO);
            }
        }

        return marketTemplateList;
    }

    @Override
    public ARMessageTemplateVO getARMessageTemplateVO(String marketCode, String templateCode, String langCode) {
        String templateKey = generateTemplateKey(marketCode, templateCode, langCode);
        myLog.debug("Find ARMessageTemplateVO for: " + templateKey);

        ARMessageTemplateVO templateVO = messageTemplates.get(templateKey);
        if (templateVO != null) {
            return templateVO;
        } else {
            myLog.debug("Can't find ARMessageTemplateVO for: " + templateCode + ", try to use the default one");
            templateVO = messageTemplates.get(generateTemplateKey(marketCode, templateCode, defaultLangCode));

            if (templateCode != null) {
                return templateVO;
            } else {
                myLog.error("Can't find default ARMessageTemplateVO for: " + templateCode);
                return null;
            }
        }
    }

    @Override
    public String getTemplateMessageText(String marketCode, String templateCode, String langCode, ARMessageTemplateField arMessageTemplateField) {
        ARMessageTemplateVO templateVO = getARMessageTemplateVO(marketCode, templateCode, langCode);
        if (templateVO == null || StringUtils.isEmpty(templateVO.getTemplateValue(getEngineName(), arMessageTemplateField))) {
            templateVO = getARMessageTemplateVO(marketCode, templateCode, defaultLangCode);
            if (templateVO == null || StringUtils.isEmpty(templateVO.getTemplateValue(getEngineName(), arMessageTemplateField))) {
                myLog.error("Can't find default template message for: " + templateCode + ", field: " + arMessageTemplateField);
                return null;
            }
        }

        return templateVO.getTemplateValue(getEngineName(), arMessageTemplateField);
    }

    @Override
    public String getProcessedMessageText(String marketCode, String templateCode, String langCode, ARMessageTemplateField arMessageTemplateField, Map<String, Object> attributes) {
        return getTemplateMessageText(marketCode, templateCode, langCode, arMessageTemplateField);
    }

    protected String generateTemplateKey(String marketCode, String templateCode, String langCode) {
        return marketCode + "#" + templateCode + "#" + langCode;
    }
}
